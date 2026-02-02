# Builds for the target platform or both if no target is specified
build target="":
    if [ -z "{{target}}" ]; then \
        ./gradlew build; \
    else \
        ./gradlew :{{target}}:build; \
    fi
