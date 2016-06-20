package org.jsonstat.v1;

import org.jsonstat.v1.parser.JacksonStatParser;
import org.jsonstat.v1.table.CsvRenderer;
import org.jsonstat.v1.table.Table;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: erlhamna
 * Date: 16.10.13
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        oecd();
        if (args.length == 1) {
            Stat stat = new JacksonStatParser().parse(new FileInputStream(args[0]));
            List<Dataset> datasets = stat.getDatasets();
            for (Dataset ds : datasets) {
                System.out.println("ds.size() = " + ds.size());

                Table table = Table.fromDataset(ds, ds.getDimension("locality").get());
                String csv = table.render(new CsvRenderer());
                System.out.println("csv =\n " + csv);
            }

        }
        /*Stat stat = new JacksonStatParser().parse(Main.class.getResourceAsStream("/oecd-canada.json"));
        Optional<Dataset> dataset = stat.getDataset(0);
        for (Dataset ds: dataset) {
            String render = Table.fromDataset(ds).render(new CsvRenderer());
            System.out.println(render);
        }*/
    }

    private static void oecd() throws IOException {
        Stat stat = new JacksonStatParser().parse(Main.class.getResourceAsStream("/oecd-canada.json"));
        Optional<Dataset> canada = stat.getDataset("canada");
        if (canada.isPresent()) {
            Dataset set = canada.get();
            System.out.println("size = " + set.size());
            Data value = set.getValue(44);
            System.out.println("value = " + value);
            Data value2 = set.getValue(new int[]{0, 0, 7, 0, 2});
            System.out.println("value2 = " + value2);
            Data value3 = set.getValue(new HashMap<String, String>(){
                {
                    put("country", "CA");
                    put("year", "2012");
                    put("concept", "POP");
                    put("age", "34");
                    put("sex", "F");
                }
            });
            System.out.println("value3 = " + value3);
            Data value4 = set.getValue(new HashMap<String, String>(){
                {
                    put("concept", "POP");
                    put("age", "34");
                    put("sex", "F");
                }
            });
            System.out.println("value4 = " + value4);

            List<Data> value5 = set.getSlice(new HashMap<String, String>(){
                {
                    put("concept", "POP");
                    put("age", "34");
              //      put("sex", "F");
                }
            });
            System.out.println("value5 = " + value5);
            List<Data> value6 = set.getSlice(new HashMap<String, String>(){
                {
                    put("concept", "PERCENT");
                    put("age", "34");
              //      put("sex", "F");
                }
            });
            System.out.println("value6 = " + value6);

            Table table = Table.fromDataset(set);

            String csv = table.render(new CsvRenderer());
            System.out.println(csv);
        }
    }

}
