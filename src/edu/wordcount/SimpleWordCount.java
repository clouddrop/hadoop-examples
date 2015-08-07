package edu.wordcount;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class SimpleWordCount {

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntWritable> {

		//1, "one tow three" -> one, 1 and two ,1 and three,1
		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {

			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);

			while (tokenizer.hasMoreTokens()) {
				String keyFromData =tokenizer.nextToken();
				value.set(keyFromData);
				Text newValue = new Text(keyFromData);
				output.collect(newValue, new IntWritable(1));

				// // I am fine I am fine
				// v
				// I 1
				// am 1
				// fine 1
				// I 1
				// am 1
				// fine 1

				// I (1,1)
				System.out.println("@Map adding" + keyFromData + " value " + " 1");
			}

		}
	}

	

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {

			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
				// sum = sum + 1;
			}

			// beer,3

			output.collect(key, new IntWritable(sum));
			System.out.println("@Reduce adding" + key + " value " + sum);
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = new JobConf(SimpleWordCount.class);
		conf.setJobName("simplewordcount");


		
		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		//how the data will be read
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		
		RunningJob runningJob = JobClient.runJob(conf);
		Counters conters  = runningJob.getCounters();
		conters.getGroupNames();
		

	}
}
