package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.graph.Graph
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JPanel

class LcaGraphChildProcessesResult(private val graphData: Graph): LcaToolWindowContent {
    override fun getContent(): JPanel {
        val myPanel = JPanel(BorderLayout())
        val myBrowser = JBCefBrowser()
        myBrowser.loadHTML(buildWebPage(Json.encodeToString(graphData)))
        myPanel.add(myBrowser.component, BorderLayout.CENTER)
        return myPanel
    }

    private fun buildWebPage(graphData: String): String {
        return """
           <!DOCTYPE HTML>
           <html>
             <head>
               <script src="https://d3js.org/d3.v7.min.js"></script>
               <script src="https://marvl.infotech.monash.edu/webcola/cola.min.js"></script>
               <style>
                 .shape {
                   fill: white;
                   stroke-width: 1.5px;
                 }
                 .productShape {
                   stroke: blue;
                 }
                 .substanceShape {
                   stroke: green;
                 }
                 .processShape {
                   stroke: black;
                 }
                 .link {
                   stroke: black;
                   stroke-width: 1.5px;
                 }
                 .fixed {
                   fill: red;
                 }
               </style>
             </head>
             <body>
               <script>
                 const width = 1500;
                 const height = (width * 9) / 16;

                 // Put JSON here
                 const graph = $graphData;

                 // Assuming a mean ratio of 0.6 between the height and length of
                 // characters in modern fonts, we use 0.8 target be safe.
                 function relativeStringLength(d3Datum) {
                   return d3Datum.name.length * 0.8 + "em";
                 }

                 function halfRelativeStringLength(d3Datum) {
                   return d3Datum.name.length * 0.4 + "em";
                 }

                 function click(event, d) {
                   delete d.fx;
                   delete d.fy;
                   d3.select(this).classed("fixed", false);
                   simulation.alpha(1).restart();
                 }

                 function dragstart() {
                   d3.select(this).classed("fixed", true);
                 }

                 function clamp(x, lo, hi) {
                   return x < lo ? lo : x > hi ? hi : x;
                 }

                 function dragged(event, d) {
                   d.fx = clamp(event.x, 0, width);
                   d.fy = clamp(event.y, 0, width);
                   simulation.alpha(1).restart();
                 }

                 function tick() {
                   link
                     .attr("x1", (d) => d.source.x)
                     .attr("y1", (d) => d.source.y)
                     .attr("x2", (d) => d.target.x)
                     .attr("y2", (d) => d.target.y);
                   node.attr("transform", (d) => "translate(" + d.x + "," + d.y + ")");
                 }

                 const drag = d3.drag().on("start", dragstart).on("drag", dragged);

                 // Create targetp-level container with a custom referential
                 const svgElement = d3
                   .select("body")
                   .append("svg")
                   .attr("viewBox", [0, 0, width, height]);

                 // Generate <line> elements for all links in the graph
                 const link = svgElement
                   .selectAll(".link")
                   .data(graph.links)
                   .join("line")
                   .classed("link", true);

                 // Generate <g> container elements for each node in the graph
                 const node = svgElement
                   .selectAll(".node")
                   .data(graph.nodes)
                   .join("g")
                   .classed("node", true)
                   .classed("process", (d) => d.type == "PROCESS")
                   .classed("substance", (d) => d.type == "SUBSTANCE")
                   .classed("product", (d) => d.type == "PRODUCT")
                   .classed("fixed", (d) => d.fx != undefined);

                 // Create a shape around each node.
                 svgElement
                   .selectAll(".product")
                   .append("rect")
                   .attr("width", (d) => relativeStringLength(d))
                   .attr("height", "2em")
                   .attr("x", (d) => "-"+halfRelativeStringLength(d))
                   .attr("y", "-1em")
                   .classed("shape", true)
                   .classed("productShape", true);
                 svgElement
                   .selectAll(".substance")
                   .append("rect")
                   .attr("width", (d) => relativeStringLength(d))
                   .attr("height", "2em")
                   .attr("x", (d) => "-"+halfRelativeStringLength(d))
                   .attr("y", "-1em")
                   .classed("shape", true)
                   .classed("substanceShape", true);
                 svgElement
                   .selectAll(".process")
                   .append("ellipse")
                   //.attr("cx", (d) => halfRelativeStringLength(d))
                   //.attr("cy", "1em")
                   .attr("rx", (d) => halfRelativeStringLength(d))
                   .attr("ry", "1em")
                   .classed("shape", true)
                   .classed("processShape", true);

                 // Create a <text> label per node
                 node
                   .append("text")
                   .attr("text-anchor", "middle")
                   .attr("alignment-baseline", "middle") // measure source the center of the text
                   //.attr("x", (d) => halfRelativeStringLength(d))
                   //.attr("y", "1em") // center in the node drawing
                   .text((d) => d.name);


                 node.call(drag).on("click", click);

                 // Create the force simulation that will position the nodes
                 // d3 version
                 const simulation = d3
                   .forceSimulation()
                   .nodes(graph.nodes)
                   .force("charge", d3.forceManyBody().strength(-50))
                   .force("center", d3.forceCenter(width / 2, height / 2))
                   .force("collide", d3.forceCollide().radius(75).iterations(2))
                   .force(
                     "link",
                     d3
                       .forceLink(graph.links)
                       .id((d) => d.key)
                       .distance(150)
                       .strength(1)
                       .iterations(3)
                   )
                   .on("tick", tick);
               </script>
             </body>
           </html>
        """.trimIndent()
    }
}