package dev.korafx.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class RouteMatch(
    val path: String,
    val fullPath: String,
    val params: Map<String, String>,
    val query: RouteQuery,
    val hash: String?,
    val score: Int = 0,
)

object RoutePattern {
    fun normalize(path: String): String {
        val value = path.trim().ifBlank { "/" }
        val prefixed = if (value.startsWith("/")) value else "/$value"
        return if (prefixed.length > 1) {
            prefixed.trimEnd('/')
        } else {
            prefixed
        }
    }

    fun match(
        pattern: String,
        location: String,
    ): RouteMatch? {
        val parsed = parseLocation(location)
        val patternSegments = normalize(pattern).segments()
        val pathSegments = parsed.path.segments()
        val matched = matchSegments(
            patternSegments = patternSegments,
            pathSegments = pathSegments,
            patternIndex = 0,
            pathIndex = 0,
            params = emptyMap(),
            score = if (patternSegments.isEmpty()) ROOT_SCORE else 0,
        ) ?: return null

        return RouteMatch(
            path = parsed.path,
            fullPath = parsed.fullPath,
            params = matched.params,
            query = parsed.query,
            hash = parsed.hash,
            score = matched.score,
        )
    }

    fun build(
        pattern: String,
        params: Map<String, Any?> = emptyMap(),
        query: Map<String, Any?> = emptyMap(),
        hash: String? = null,
    ): String {
        val pathSegments = normalize(pattern).segments().mapNotNull { segment ->
            when {
                segment == "*" -> {
                    params["*"]?.toString()?.trim('/')?.takeIf(String::isNotBlank)?.let(::encodeSplat)
                }

                segment.startsWith(":") -> {
                    val optional = segment.endsWith("?")
                    val name = segment.removePrefix(":").removeSuffix("?")
                    val value = params[name]
                    when {
                        value == null && optional -> null
                        value == null -> error("Missing required route param '$name'.")
                        else -> encode(value.toString())
                    }
                }

                segment.endsWith("?") -> segment.removeSuffix("?")

                else -> segment
            }
        }
        val path = "/" + pathSegments.joinToString("/")
        val normalizedPath = if (path == "/") "/" else path.trimEnd('/')
        val queryString = buildQuery(query)

        return buildString {
            append(normalizedPath)
            if (queryString.isNotBlank()) {
                append("?")
                append(queryString)
            }
            hash?.takeIf(String::isNotBlank)?.let {
                append("#")
                append(encode(it))
            }
        }
    }

    fun parseLocation(location: String): RouteMatch {
        val fullPath = location.ifBlank { "/" }
        val hashSplit = fullPath.split("#", limit = 2)
        val beforeHash = hashSplit[0]
        val rawHash = hashSplit.getOrNull(1)?.takeIf(String::isNotBlank)
        val hash = rawHash?.let(::decode)
        val querySplit = beforeHash.split("?", limit = 2)
        val path = normalize(querySplit[0])
        val query = querySplit.getOrNull(1)?.let(::parseQuery) ?: RouteQuery.Empty

        return RouteMatch(
            path = path,
            fullPath = buildString {
                append(path)
                querySplit.getOrNull(1)?.takeIf(String::isNotBlank)?.let {
                    append("?")
                    append(it)
                }
                rawHash?.let {
                    append("#")
                    append(it)
                }
            },
            params = emptyMap(),
            query = query,
            hash = hash,
        )
    }

    private fun String.segments(): List<String> =
        trim('/').takeIf(String::isNotBlank)?.split("/") ?: emptyList()

    private fun parseQuery(query: String): RouteQuery {
        val values = linkedMapOf<String, MutableList<String>>()
        query.split("&")
            .filter(String::isNotBlank)
            .forEach { pair ->
                val parts = pair.split("=", limit = 2)
                val key = decode(parts[0])
                val value = decode(parts.getOrElse(1) { "" })
                values.getOrPut(key) { mutableListOf() } += value
            }

        return RouteQuery(values)
    }

    private fun matchSegments(
        patternSegments: List<String>,
        pathSegments: List<String>,
        patternIndex: Int,
        pathIndex: Int,
        params: Map<String, String>,
        score: Int,
    ): SegmentMatch? {
        if (patternIndex == patternSegments.size) {
            return if (pathIndex == pathSegments.size) {
                SegmentMatch(params, score)
            } else {
                null
            }
        }

        val segment = patternSegments[patternIndex]
        return when {
            segment == "*" -> matchSplat(
                patternSegments = patternSegments,
                pathSegments = pathSegments,
                patternIndex = patternIndex,
                pathIndex = pathIndex,
                params = params,
                score = score,
            )

            segment.startsWith(":") -> matchDynamicSegment(
                segment = segment,
                patternSegments = patternSegments,
                pathSegments = pathSegments,
                patternIndex = patternIndex,
                pathIndex = pathIndex,
                params = params,
                score = score,
            )

            segment.endsWith("?") -> matchOptionalStaticSegment(
                segment = segment.removeSuffix("?"),
                patternSegments = patternSegments,
                pathSegments = pathSegments,
                patternIndex = patternIndex,
                pathIndex = pathIndex,
                params = params,
                score = score,
            )

            pathSegments.getOrNull(pathIndex)?.let(::decode) == segment ->
                matchSegments(
                    patternSegments = patternSegments,
                    pathSegments = pathSegments,
                    patternIndex = patternIndex + 1,
                    pathIndex = pathIndex + 1,
                    params = params,
                    score = score + STATIC_SCORE,
                )

            else -> null
        }
    }

    private fun matchSplat(
        patternSegments: List<String>,
        pathSegments: List<String>,
        patternIndex: Int,
        pathIndex: Int,
        params: Map<String, String>,
        score: Int,
    ): SegmentMatch? =
        (pathIndex..pathSegments.size)
            .mapNotNull { nextPathIndex ->
                val splat = pathSegments
                    .subList(pathIndex, nextPathIndex)
                    .joinToString("/") { value -> decode(value) }
                matchSegments(
                    patternSegments = patternSegments,
                    pathSegments = pathSegments,
                    patternIndex = patternIndex + 1,
                    pathIndex = nextPathIndex,
                    params = params + ("*" to splat),
                    score = score + SPLAT_SCORE,
                )
            }
            .maxByOrNull(SegmentMatch::score)

    private fun matchDynamicSegment(
        segment: String,
        patternSegments: List<String>,
        pathSegments: List<String>,
        patternIndex: Int,
        pathIndex: Int,
        params: Map<String, String>,
        score: Int,
    ): SegmentMatch? {
        val optional = segment.endsWith("?")
        val name = segment.removePrefix(":").removeSuffix("?")
        val value = pathSegments.getOrNull(pathIndex)
        val consume = value?.let {
            matchSegments(
                patternSegments = patternSegments,
                pathSegments = pathSegments,
                patternIndex = patternIndex + 1,
                pathIndex = pathIndex + 1,
                params = params + (name to decode(it)),
                score = score + if (optional) OPTIONAL_DYNAMIC_SCORE else DYNAMIC_SCORE,
            )
        }
        val skip =
            if (optional) {
                matchSegments(
                    patternSegments = patternSegments,
                    pathSegments = pathSegments,
                    patternIndex = patternIndex + 1,
                    pathIndex = pathIndex,
                    params = params,
                    score = score + OPTIONAL_MISSING_SCORE,
                )
            } else {
                null
            }

        return listOfNotNull(consume, skip).maxByOrNull(SegmentMatch::score)
    }

    private fun matchOptionalStaticSegment(
        segment: String,
        patternSegments: List<String>,
        pathSegments: List<String>,
        patternIndex: Int,
        pathIndex: Int,
        params: Map<String, String>,
        score: Int,
    ): SegmentMatch? {
        val consume =
            if (pathSegments.getOrNull(pathIndex)?.let(::decode) == segment) {
                matchSegments(
                    patternSegments = patternSegments,
                    pathSegments = pathSegments,
                    patternIndex = patternIndex + 1,
                    pathIndex = pathIndex + 1,
                    params = params,
                    score = score + OPTIONAL_STATIC_SCORE,
                )
            } else {
                null
            }
        val skip = matchSegments(
            patternSegments = patternSegments,
            pathSegments = pathSegments,
            patternIndex = patternIndex + 1,
            pathIndex = pathIndex,
            params = params,
            score = score + OPTIONAL_MISSING_SCORE,
        )

        return listOfNotNull(consume, skip).maxByOrNull(SegmentMatch::score)
    }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun buildQuery(query: Map<String, Any?>): String =
        query.flatMap { (key, value) ->
            when (value) {
                null -> emptyList()
                is Iterable<*> -> value.filterNotNull().map { item -> key to item }
                is Array<*> -> value.filterNotNull().map { item -> key to item }
                else -> listOf(key to value)
            }
        }.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value.toString())}"
        }

    private fun encodeSplat(value: String): String =
        encode(value).replace("%2F", "/")

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20")

    private const val STATIC_SCORE = 100
    private const val OPTIONAL_STATIC_SCORE = 80
    private const val DYNAMIC_SCORE = 50
    private const val OPTIONAL_DYNAMIC_SCORE = 30
    private const val OPTIONAL_MISSING_SCORE = 5
    private const val ROOT_SCORE = 1
    private const val SPLAT_SCORE = -100
}
