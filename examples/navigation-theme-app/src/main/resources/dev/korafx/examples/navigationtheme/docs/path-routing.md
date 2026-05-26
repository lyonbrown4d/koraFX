# Path Routing

`PathRoute` 通过 `path` 定义地址规则，`navigatePath` 与 `matchPath` 可用于字符串级导航。

### 示例

- `"/projects/:projectId/:tab?"` 匹配 `/projects/42` 与 `/projects/42/files`
- `"/files/*"` 匹配任意后缀路径
- 支持 query / hash，例如 `/projects/42/files?sort=asc#section`

```text
/projects/42
/projects/42/files?mode=edit#row-2
/files/src/main/App.kt
```

### 代码要点

1. `Navigator.matchPath(path)` 返回 `NavigationLocation`（带 `params`、`query`、`hash`）。
2. `navigatePath` 会做匹配并触发 guard。
3. `NavigationLocation.withQuery/withHash/withoutQuery` 方便衍生新路径。
