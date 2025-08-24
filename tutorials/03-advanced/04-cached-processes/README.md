# Using cached processes

When you run an inventory or impact assessment, the engine builds a system of equations that represents all processes and their dependencies.  
This system is then solved in one pass to compute the results.

For small models this is fine, but for large models the system can grow very large, making the solve more costly.

The `@cached` annotation helps here: it tells the engine to **pre-solve a subsystem** (a process and its dependencies) during runtime.  
The result of this local solve is then reused in the global assessment.  
This makes the final system smaller and the overall computation faster while giving exactly the same results.

## Try it yourself

The file `main.lca` defines a `bake` process annotated with `@cached`.

Run the following command once with the annotation, and once without:

```bash
lcaac trace sandwich_factory
```

Compare the two results:

Without `@cached`, you will see `flour_production` and `salt_production` listed as dependencies of `sandwich_factory`.

With `@cached`, these dependencies disappear from the trace. They are pre-solved and collapsed into the `bake` process.

The impacts remain the same in both cases. Only the solving pipeline changes.