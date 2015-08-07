package edu.kv;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;

import org.apache.hadoop.mapred.KeyValueTextInputFormat;

/**
 * Demonstate the KeyValueTextInputFormat use
 * the input line is automatically broken in key and value.
 * In case of weather data .. they key would be weather station data and remaining value
 * @author samar
 *
 */
public class KeyValuesClass {
//key1 "today is good"
//key2 "yesterday was good"
	public static class Map extends MapReduceBase implements
			Mapper<Text, Text, Text, IntWritable> {

		//
		@Override
		public void map(Text key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {

			String values = value.toString().trim();
			int word = Integer.parseInt(values);
			output.collect(key, new IntWritable(word));

		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterator<IntWritable> values,OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}

	}

	public static void main(String[] args) throws Exception {

		// Example file - Temperature data

		JobConf conf = new JobConf(KeyValuesClass.class);
		conf.setJobName("KeyValueClass");

		// Setting this property to use KeyValueInput Format
		// Everything before a space will count as key and rest as value
		conf.set("key.value.separator.in.input.line", " ");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(KeyValueTextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);

	}
}
