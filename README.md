## Conway's Game of Life

### Development
```
BellSoft Liberica 25 Full 25.0.4+9
```
```
native-image 25.0.4 2026-07-21
GraalVM Runtime Environment Liberica-NIK-25.0.4-1 (build 25.0.4+10-LTS)
Substrate VM Liberica-NIK-25.0.4-1 (build 25.0.4+10-LTS, serial gc)
```

All code in this repository was written by AI.

The Windows native executable hits this bug https://github.com/bell-sw/LibericaNIK/issues/37

The jar works
```java --enable-native-access=javafx.graphics -jar target\lifejfx-1.0.0.jar```

### Game references

Peter Norvig's notebook [Life.ipynb](https://github.com/norvig/pytudes/blob/main/ipynb/Life.ipynb) contains a concise explanation of rules with Python code.

