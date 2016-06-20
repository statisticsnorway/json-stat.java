package org.jsonstat.v1.table;

import org.jsonstat.v1.Data;

import java.util.List;

public class CsvRenderer implements Renderer<String> {
    @Override
    public String render(Table table) {
        StringBuilder headerBuilder = new StringBuilder();
        StringBuilder bodyBuilder = new StringBuilder();
        List<TableHeader> headers = table.getHeaders();
        headerBuilder.append("#");
        for (int i1 = 0; i1 < headers.size(); i1++) {
            TableHeader header = headers.get(i1);
            headerBuilder.append(header.getLabel().orElseGet(() -> header.getId().orElse(null)));
            if (i1 < headers.size()-1) {
                headerBuilder.append(",");
            }
        }
        headerBuilder.append("\n");
        List<List<Data>> rows = table.getRows();
        for (List<Data> row : rows) {
            for(int i = 0; i < row.size(); i++) {
                Data data = row.get(i);
                bodyBuilder.append(data.toString());
                if (i < row.size()-1) {
                    bodyBuilder.append(",");
                }
            }
            bodyBuilder.append("\n");
        }
        headerBuilder.append(bodyBuilder);
        return headerBuilder.toString();
    }
}
