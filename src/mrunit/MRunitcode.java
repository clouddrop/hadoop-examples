package mrunit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import samar.wordcount.WordCount;

public class MRunitcode extends TestCase {

	public static class MapTest extends Mapper<LongWritable, Text, Text, IntWritable> {
		Text day = new Text();

		public void map(LongWritable key, Text value, Context ct) throws IOException,
				InterruptedException {
			String[] line = value.toString().split(",");
			int val = Integer.parseInt(line[0]);
			day.set(line[1]);
			ct.write(day, new IntWritable(val));

		}
	}

	MapDriver<LongWritable, Text, Text, IntWritable> mapDriver;
	ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
	MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

	public void setUp() {
		new MapTest();
		// mapDriver = MapDriver.newMapDriver(new MapTest());
		mapDriver = MapDriver.newMapDriver(new WordCount.TokenizerMapper());
		reduceDriver = ReduceDriver.newReduceDriver(new WordCount.IntSumReducer());
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(new WordCount.TokenizerMapper(),
				new WordCount.IntSumReducer());

	}

	@Test
	public void testMapper() {
		try {
			mapDriver.withInput(new LongWritable(), new Text("sunday sunday holiday"))
					.withOutput(new Text("sunday"), new IntWritable(1))
					.withOutput(new Text("sunday"), new IntWritable(1))
					.withOutput(new Text("holiday"), new IntWritable(2)).runTest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testMapReduce() throws Exception {
		mapReduceDriver.withInput(new LongWritable(), new Text(
				"sunday sunday holiday"));
		
		List<IntWritable> values = new ArrayList<IntWritable>();
		values.add(new IntWritable(1));
		values.add(new IntWritable(1));
		
		mapReduceDriver
		.withOutput(new Text("holiday"), new IntWritable(1))
		.withOutput(new Text("sunday"), new IntWritable(2));
		mapReduceDriver.runTest();
	}
}
