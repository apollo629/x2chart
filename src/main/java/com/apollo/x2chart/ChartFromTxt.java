package com.apollo.x2chart;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by alpereninal on 01/06/15.
 */
public class ChartFromTxt {

    @Option(name="-i",usage="input file", metaVar="INPUT")
    private static File input;

    @Option(name="-o",usage="output filename",metaVar="OUTPUT")
    private String out;

    @Option(name="-d",usage="delimeter", metaVar="INPUT")
    private static String delimeter = ",";

    @Option(name="-t",usage="title", metaVar="INPUT")
    private static String title = "title";

    @Option(name="-x",usage="xAxis", metaVar="INPUT")
    private static String xAxis = "xAxis";

    @Option(name="-y",usage="yAxis", metaVar="INPUT")
    private static String yAxis = "yAxis";

    @Option(name="-c",usage="Chart Type bar=1 pie=2", metaVar="INPUT")
    private static int chartType = 0;

    public static void main(String[] args) {
        new ChartFromTxt().doMain(args);
    }

    public void doMain(String[] args){
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);
            if( args.length == 0 ) {
                throw new CmdLineException(parser, "No argument is given", new Throwable("No argument is given"));
            }
            Path path = Paths.get(input.getAbsolutePath());
            List<String> headers = readHeader(path);

            Stream<String> lines = Files.lines(path);
            List<String> numbers = (lines.skip(1).map(line -> line.split(",")).flatMap(Arrays::stream).collect(toList()));

            if (chartType == 0)
                throw new CmdLineException(parser, "Please choose a chart type", new Throwable("No chart type is choosen"));
            if (chartType == 1)
                writeChartToPDF(createBarChart(numbers, headers), 500, 400, out);
            if (chartType == 2)
                writeChartToPDF(createPieChart(numbers, headers), 500, 400, out);

            System.out.println("Chart pdf is created successfully in path: " + new File(out).getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                try {
                    File myFile = new File(out);
                    Desktop.getDesktop().open(myFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // no application registered for PDFs
                }
            }

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    List<String> readHeader(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.findFirst()
                    .map(line -> Arrays.asList(line.split(delimeter)))
                    .get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static JFreeChart createBarChart(List<String> dataList, List<String> headers){
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        int index = 0;
        for(String s : dataList) {
            dataSet.setValue(Integer.valueOf(s), "rowKey", headers.get(index));
            index++;
        }
        return ChartFactory.createBarChart(title,xAxis,yAxis, dataSet, PlotOrientation.VERTICAL, false, true, false);
    }

    static JFreeChart createPieChart(List<String> dataList, List<String> headers){
        DefaultPieDataset dataSet = new DefaultPieDataset();
        int index = 0;
        for(String s : dataList) {
            dataSet.setValue(headers.get(index), Integer.valueOf(s));
            index++;
        }
        return ChartFactory.createPieChart(title, dataSet, true, true, false);
    }

    static void writeChartToPDF(JFreeChart chart, int width, int height, String fileName){
        PdfWriter writer = null;
        Document document = new Document();
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            PdfContentByte contentByte = writer.getDirectContent();
            PdfTemplate template = contentByte.createTemplate(width, height);
            Graphics2D graphics2d = template.createGraphics(width, height,new DefaultFontMapper());
            Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,height);
            chart.draw(graphics2d, rectangle2d);
            graphics2d.dispose();
            contentByte.addTemplate(template, 0, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        document.close();
    }

}
