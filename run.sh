#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

build() {
    rm -rf out
    mkdir -p out
    javac --release 17 -d out $(find src -name "*.java")
}

case "${1:-all}" in
    build) build ;;
    test) build && java -cp out textanalysis.TestRunner ;;
    demo) build && java -cp out textanalysis.Main ;;
    bench) build && java -Xms512m -cp out textanalysis.bench.Benchmark results ;;
    plots) python3 scripts/plot_results.py ;;
    all)
        build
        java -cp out textanalysis.TestRunner
        java -cp out textanalysis.Main
        java -Xms512m -cp out textanalysis.bench.Benchmark results
        python3 scripts/plot_results.py
        ;;
    *) echo "usage: ./run.sh [build|test|demo|bench|plots|all]"; exit 1 ;;
esac
