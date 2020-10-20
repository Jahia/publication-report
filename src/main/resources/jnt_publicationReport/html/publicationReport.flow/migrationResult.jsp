<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<script type="text/javascript">
    function downloadCSV(csv, filename) {
        var csvFile;
        var downloadLink;

        csvFile = new Blob([csv], {type: "text/csv"});
        downloadLink = document.createElement("a");
        downloadLink.download = filename;
        downloadLink.href = window.URL.createObjectURL(csvFile);
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
        downloadLink.click();
    }

    function exportTableToCSV(filename) {
        var csv = [];
        var rows = document.querySelectorAll("table tr");

        for (var i = 0; i < rows.length; i++) {
            var row = [], cols = rows[i].querySelectorAll("td, th");

            for (var j = 0; j < cols.length; j++)
                row.push(cols[j].innerText);

            csv.push(row.join(","));
        }

        downloadCSV(csv.join("\n"), filename);
    }
</script>

<div class="box-1">

    <h1>Unpublished Nodes Report</h1>

    <form:form modelAttribute="environmentInfo" class="form-horizontal" method="post">

        <button id="previous" class="btn btn-primary" type="submit" name="_eventId_previous">
            Back
        </button>
        <button id="export" class="btn btn-primary" type="submit" name="_eventId_export"
                onclick="exportTableToCSV('members.csv')">Export Report to CSV
        </button>
    </form:form>

    <c:set var="redirectUrl" value="${renderContext.mainResource.node.path}.html" scope="session"/>

    <table class="table table-striped">
        <thead>
        <tr>
            <th>Node Path</th>
            <th>UUID</th>
            <th>Publication Status</th>
            <th>Author</th>
            <th>Last Modified</th>
            <th>Locale</th>
        </tr>
        </thead>
        <c:forEach items="${migrationReport}" var="module">
            <tr>
                <td>${module.nodePath}</td>
                <td>${module.uuid}</td>
                <td>${module.publishStatus}</td>
                <td>${module.author}</td>
                <td>${module.lastModified}</td>
                <td>${module.locale}</td>
            </tr>
        </c:forEach>
    </table>

</div>
