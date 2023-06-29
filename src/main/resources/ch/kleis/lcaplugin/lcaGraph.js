import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import * as d3Sankey from "https://cdn.jsdelivr.net/npm/d3-sankey@0.12.3/+esm";

// Utils
var count = 0;

export function uid(name) {
    return new Id("O-" + (name == null ? "" : name + "-") + ++count);
}

function Id(id) {
    this.id = id;
    this.href = new URL(`#${id}`, location) + "";
}

Id.prototype.toString = function () {
    return "url(" + this.href + ")";
};

// Specify the dimensions of the chart.
const width = 928;
const height = 600;
const format = d3.format(",.0f");

// Create a SVG container.
const svg = d3.create("svg")
    .attr("width", width)
    .attr("height", height)
    .attr("viewBox", [0, 0, width, height])
    .attr("style", "max-width: 100%; height: auto; font: 10px sans-serif;");

// Constructs and configures a Sankey generator.
const sankey = d3Sankey.sankey()
    .nodeId(d => d.name)
    .nodeAlign(d3Sankey.sankeyJustify)
    .nodeWidth(15)
    .nodePadding(10)
    .extent([[1, 5], [width - 1, height - 5]]);

// Applies it to the data. We make a copy of the nodes and links objects
// to avoid mutating the original.
const {nodes, links} = sankey({
    nodes: data.nodes.map(d => Object.assign({}, d)),
    links: data.links.map(d => Object.assign({}, d)),
});

// Defines a color scale.
const color = d3.scaleOrdinal(d3.schemeCategory10);

// Creates the rects that represent the nodes.
const rect = svg.append("g")
    .attr("stroke", "#000")
    .selectAll()
    .data(nodes)
    .join("rect")
    .attr("x", d => d.x0)
    .attr("y", d => d.y0)
    .attr("height", d => d.y1 - d.y0)
    .attr("width", d => d.x1 - d.x0)
    .attr("fill", d => color(d.name));

// Adds a title on the nodes.
rect.append("title")
    .text(d => `${d.name}\n${format(d.value)} TWh`);

// Creates the paths that represent the links.
const link = svg.append("g")
    .attr("fill", "none")
    .attr("stroke-opacity", 0.5)
    .selectAll()
    .data(links)
    .join("g")
    .style("mix-blend-mode", "multiply");


// Creates a gradient, if necessary, for the source-target color option.
const gradient = link.append("linearGradient")
    .attr("id", d => (d.uid = uid("link")).id)
    .attr("gradientUnits", "userSpaceOnUse")
    .attr("x1", d => d.source.x1)
    .attr("x2", d => d.target.x0);
gradient.append("stop")
    .attr("offset", "0%")
    .attr("stop-color", d => color(d.source));
gradient.append("stop")
    .attr("offset", "100%")
    .attr("stop-color", d => color(d.target));


link.append("path")
    .attr("d", d3Sankey.sankeyLinkHorizontal())
    .attr("stroke", (d) => d.uid)
    .attr("stroke-width", d => Math.max(1, d.width));

link.append("title")
    .text(d => `${d.source.name} → ${d.target.name}\n${format(d.value)} TWh`);

// Adds labels on the nodes.
svg.append("g")
    .selectAll()
    .data(nodes)
    .join("text")
    .attr("x", d => d.x0 < width / 2 ? d.x1 + 6 : d.x0 - 6)
    .attr("y", d => (d.y1 + d.y0) / 2)
    .attr("dy", "0.35em")
    .attr("text-anchor", d => d.x0 < width / 2 ? "start" : "end")
    .text(d => d.name);

container.append(svg.node())

window.link = link
