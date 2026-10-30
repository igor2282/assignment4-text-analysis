import csv
import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "results"
PLOTS = ROOT / "plots"

SURFACE = "#fcfcfb"
TEXT_PRIMARY = "#1f1f1e"
TEXT_SECONDARY = "#5f5e5a"
GRID = "#e6e5e0"
SERIES = ["#2a78d6", "#eb6834", "#1baf7a"]
MARKERS = ["o", "s", "^"]


def read_rows(name):
    with open(RESULTS / name, newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def style_axes(ax, title, xlabel, ylabel):
    ax.set_facecolor(SURFACE)
    ax.set_title(title, color=TEXT_PRIMARY, fontsize=11, loc="left", pad=10)
    ax.set_xlabel(xlabel, color=TEXT_SECONDARY, fontsize=9)
    ax.set_ylabel(ylabel, color=TEXT_SECONDARY, fontsize=9)
    ax.grid(True, color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(GRID)
    ax.tick_params(colors=TEXT_SECONDARY, labelsize=8)


def draw_series(ax, xs, ys, index, label, annotate=True):
    ax.plot(xs, ys, color=SERIES[index], linewidth=2, marker=MARKERS[index], markersize=6,
            markeredgecolor=SURFACE, markeredgewidth=1.5, label=label)
    if annotate:
        ax.annotate(label, (xs[-1], ys[-1]), xytext=(6, 0), textcoords="offset points",
                    color=TEXT_PRIMARY, fontsize=8, va="center")


def new_figure(width, height, columns=1):
    fig, axes = plt.subplots(1, columns, figsize=(width, height), facecolor=SURFACE)
    return fig, axes


def plot_dp():
    rows = read_rows("dp_results.csv")
    fig, axes = new_figure(11, 4.2, columns=2)
    for index, name in enumerate(["LCS", "EditDistance"]):
        data = [r for r in rows if r["algorithm"] == name]
        label = "LCS" if name == "LCS" else "Edit Distance"
        xs = [int(r["n"]) for r in data]
        draw_series(axes[0], xs, [float(r["avg_ms"]) for r in data], index, label, annotate=False)
        draw_series(axes[1], xs, [int(r["avg_ns"]) / int(r["cells"]) for r in data], index, label, annotate=False)
    style_axes(axes[0], "Average time vs input size (n = m)", "string length n", "average time (ms)")
    style_axes(axes[1], "Time per DP cell (time / n\u00b2)", "string length n", "ns per cell")
    for ax in axes:
        ax.set_xlim(0, 1100)
        ax.set_xticks([100, 500, 1000])
        ax.set_ylim(bottom=0)
    handles, labels = axes[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc="upper center", ncol=2, frameon=False, fontsize=9, labelcolor=TEXT_PRIMARY)
    fig.tight_layout(rect=(0, 0, 1, 0.9))
    fig.savefig(PLOTS / "dp_time_vs_n.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def plot_matching():
    rows = read_rows("matching_results.csv")
    scenarios = ["present", "absent", "worst-case"]
    titles = {"present": "Pattern present (random text)", "absent": "Pattern absent (random text)",
              "worst-case": "Worst case (A...A, A^15C)"}
    algorithms = ["Naive", "KMP", "Rabin-Karp"]
    fig, axes = new_figure(13, 4.2, columns=3)
    top = max(float(r["avg_ms"]) for r in rows) * 1.12
    for ax, scenario in zip(axes, scenarios):
        for index, algorithm in enumerate(algorithms):
            data = [r for r in rows if r["scenario"] == scenario and r["algorithm"] == algorithm]
            xs = [int(r["text_length"]) for r in data]
            ys = [float(r["avg_ms"]) for r in data]
            draw_series(ax, xs, ys, index, algorithm, annotate=False)
        style_axes(ax, titles[scenario], "text length n", "average time (ms)")
        ax.set_ylim(0, top)
        ax.set_xlim(0, 115_000)
        ax.set_xticks([10_000, 50_000, 100_000])
        ax.set_xticklabels(["10k", "50k", "100k"])
    handles, labels = axes[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc="upper center", ncol=3, frameon=False, fontsize=9, labelcolor=TEXT_PRIMARY)
    fig.tight_layout(rect=(0, 0, 1, 0.92))
    fig.savefig(PLOTS / "matching_time_vs_text_length.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def plot_comparisons():
    rows = read_rows("matching_results.csv")
    scenarios = ["present", "absent", "worst-case"]
    algorithms = ["Naive", "KMP"]
    n = 100_000
    fig, ax = new_figure(8, 4.2)
    width = 0.36
    for index, algorithm in enumerate(algorithms):
        values = []
        for scenario in scenarios:
            row = next(r for r in rows if r["scenario"] == scenario and r["algorithm"] == algorithm and int(r["text_length"]) == n)
            values.append(int(row["comparisons"]) / n)
        positions = [s + (index - 0.5) * (width + 0.02) for s in range(len(scenarios))]
        bars = ax.bar(positions, values, width=width, color=SERIES[index], label=algorithm, edgecolor=SURFACE, linewidth=2)
        for bar, value in zip(bars, values):
            ax.annotate(f"{value:.2f}", (bar.get_x() + bar.get_width() / 2, value), xytext=(0, 3),
                        textcoords="offset points", ha="center", fontsize=8, color=TEXT_PRIMARY)
    style_axes(ax, "Naive vs KMP: character comparisons per text character (n = 100,000, m = 16)", "", "comparisons / n")
    ax.set_xticks(range(len(scenarios)))
    ax.set_xticklabels(scenarios)
    ax.legend(frameon=False, fontsize=8, labelcolor=TEXT_PRIMARY, loc="upper left")
    fig.tight_layout()
    fig.savefig(PLOTS / "matching_comparisons.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def plot_kasai():
    rows = read_rows("kasai_results.csv")
    xs = [int(r["n"]) for r in rows]
    fig, axes = new_figure(10, 4, columns=2)
    draw_series(axes[0], xs, [float(r["avg_ms"]) for r in rows], 0, "Kasai", annotate=False)
    style_axes(axes[0], "Kasai: average time vs n", "string length n", "average time (ms)")
    draw_series(axes[1], xs, [int(r["comparisons"]) for r in rows], 0, "Kasai", annotate=False)
    axes[1].plot(xs, [2 * x for x in xs], color=TEXT_SECONDARY, linewidth=1, linestyle="--")
    axes[1].annotate("2n", (xs[-1], 2 * xs[-1]), xytext=(6, 0), textcoords="offset points", color=TEXT_SECONDARY, fontsize=8, va="center")
    style_axes(axes[1], "Kasai: character comparisons vs n", "string length n", "comparisons")
    for ax in axes:
        ax.set_xticks(xs)
        ax.set_xlim(0, 11_000)
        ax.set_ylim(bottom=0)
    fig.tight_layout()
    fig.savefig(PLOTS / "kasai_time_vs_n.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def main():
    PLOTS.mkdir(exist_ok=True)
    plot_dp()
    plot_matching()
    plot_comparisons()
    plot_kasai()
    print("Plots written to", PLOTS)
    return 0


if __name__ == "__main__":
    sys.exit(main())
