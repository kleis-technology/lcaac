// Main {{{1
function main(width, height, graph) {
    const svgElement = createSVGElement(width, height);

    createTriangleMarkerElements(svgElement)

    const links = createLinkElements(svgElement, graph.links);
    const nodes = createNodeGElements(svgElement, graph.nodes);

    createProductNodeShapeElements(nodes);
    createSubstanceNodeShapeElements(nodes);
    createProcessNodeShapeElements(nodes);
    createNodeTextElements(nodes);

    const simulation = d3.forceSimulation();

    nodes.call(d3.drag()
        .on("start", dragstart)
        .on("drag", dragged(simulation, width))).on("click", function (_, d) { click(this, d, simulation); });

    simulation
        .nodes(graph.nodes)
        .force("charge", d3.forceManyBody().strength(-50))
        .force("center", d3.forceCenter(width / 2, height / 2))
        .force("collide", d3.forceCollide().radius(75).iterations(2))
        .force("link", d3
        .forceLink(graph.links)
        .id((d) => d.key)
        .distance(150)
        .strength(1)
        .iterations(3))
        .on("tick", () => tick(nodes, links));
}
// SVG/HTML Element creation {{{1
function createSVGElement(width, height) {
    return d3.select("body")
        .append("svg")
        .attr("viewBox", [0, 0, width, height]);
}
function createLinkElements(svgElement, links) {
    return svgElement
        .selectAll(".link")
        .data(links)
        .join("line")
        .classed("link", true)
        .attr("marker-end", "url(#triangle)");
}
function createTriangleMarkerElements(svgElement) {
    svgElement.append("marker")
        .attr("id", "triangle")
        .attr("viewBox", [0, 0, 10, 10])
        .attr("refX", 55)
        .attr("refY", 5)
        .attr("markerUnits", "strokeWidth")
        .attr("markerWidth", 10)
        .attr("markerHeight", 10)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M 0 0 L 10 5 L 0 10 z");
}
function createNodeGElements(svgElement, nodes) {
    return svgElement
        .selectAll(".node")
        .data(nodes)
        .join("g")
        .classed("node", true)
        .classed("process", (d) => d.type == "PROCESS")
        .classed("substance", (d) => d.type == "SUBSTANCE")
        .classed("product", (d) => d.type == "PRODUCT")
        .classed("fixed", (d) => d.fx != undefined);
}
function createProductNodeShapeElements(nodeGElements) {
    return nodeGElements
        .filter(".product")
        .append("rect")
        .attr("width", (d) => relativeStringLength(d))
        .attr("height", "2em")
        .attr("x", (d) => "-" + halfRelativeStringLength(d))
        .attr("y", "-1em")
        .classed("shape", true)
        .classed("productShape", true);
}
function createSubstanceNodeShapeElements(nodeGElements) {
    return nodeGElements
        .filter(".substance")
        .append("rect")
        .attr("width", (d) => relativeStringLength(d))
        .attr("height", "2em")
        .attr("x", (d) => "-" + halfRelativeStringLength(d))
        .attr("y", "-1em")
        .classed("shape", true)
        .classed("substanceShape", true);
}
function createProcessNodeShapeElements(nodeGElements) {
    return nodeGElements
        .filter(".process")
        .append("ellipse")
        .attr("rx", (d) => halfRelativeStringLength(d))
        .attr("ry", "1em")
        .classed("shape", true)
        .classed("processShape", true);
}
function createNodeTextElements(nodeGElements) {
    return nodeGElements
        .append("text")
        .attr("text-anchor", "middle")
        .attr("alignment-baseline", "middle") // measure source the center of the text
        .text((d) => d.name);
}
// Dynamic behavior callbacks {{{1
function dragstart(event, d) {
    d3.select(this).classed("fixed", true);
}
function dragged(simulation, width) {
    return (event, d) => {
        d.fx = clamp(event.x, 0, width);
        d.fy = clamp(event.y, 0, width);
        simulation.alpha(1).restart();
    };
}
function click(clickedTarget, d, simulation) {
    delete d.fx;
    delete d.fy;
    d3.select(clickedTarget).classed("fixed", false);
    simulation.alpha(1).restart();
}
function tick(nodes, links) {
    links
        .attr("x1", (d) => d.source.x)
        .attr("y1", (d) => d.source.y)
        .attr("x2", (d) => d.target.x)
        .attr("y2", (d) => d.target.y);
    nodes.attr("transform", (d) => "translate(" + d.x + "," + d.y + ")");
}
// Utility functions {{{1
function clamp(x, lo, hi) {
    return x < lo ? lo : x > hi ? hi : x;
}
// Assuming a mean ratio of 0.6 between the height and length of
// characters in modern fonts, we use 0.8 target be safe.
function relativeStringLength(d3Datum) {
    return d3Datum.name.length * 0.8 + "em";
}
function halfRelativeStringLength(d3Datum) {
    return d3Datum.name.length * 0.4 + "em";
}
// vim: set expandtab softtabstop=2 tabstop=2 :
