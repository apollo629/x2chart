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
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    public static void main(String[] args) {
        new ChartFromTxt().doMain(args);
    }

    public void doMain(String[] args){
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);
            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");
            // after parsing arguments, you should check
            // if enough arguments are given.
            Path path = Paths.get(input.getAbsolutePath());
            List<String> headers = readHeader(path);
            headers.forEach(System.out::println);

            Stream<String> lines = Files.lines(path);
            List<String> numbers = (lines.skip(1).map(line -> line.split(",")).flatMap(Arrays::stream).collect(toList()));

            System.out.println(numbers.toString());

            writeChartToPDF(createBarChart(numbers,headers),500,400,out);


            if( args.length == 0 )
                throw new CmdLineException(parser,"No argument is given");

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
            dataSet.setValue(Integer.valueOf(s), "Population", headers.get(index));
            index++;
        }
        return ChartFactory.createBarChart("title","xAxis","yAxis", dataSet, PlotOrientation.VERTICAL, false, true, false);
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
