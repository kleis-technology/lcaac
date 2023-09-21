# PSI manipulation performance

- understand distinction between PSI and ASTnodes
- know that building the AST nodes requires a lot more CPU/memory than navigating the PSI built
  during indexing
- caching of types can be added (c.f. rust/go/kotlin plugins) to improve analysis speed.


## TODO

- [ ] Remove all up-navigation in PSI (findParent), which builds the AST. Requires deep-ish reworking
  of resolution logic.
- [ ] Use AstLoadingFilter to detect where and why we load the AST, and either explicitely authorize
  or better, refactor and remove if possible. C.f.
  https://github.com/JetBrains/intellij-community/blob/idea/232.9921.47/platform/core-api/src/com/intellij/util/AstLoadingFilter.java
- [ ] cache type checker results.
