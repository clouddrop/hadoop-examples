package edu.weather;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class MaxTemperature {

	public static class MaxTemperatureMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable arg0, Text Value, OutputCollector<Text, IntWritable> output, Reporter arg3) throws IOException {

			String line = Value.toString();
			String year = line.substring(0, 4);
			int temperature = Integer.MAX_VALUE;
			int length = line.length();
			if (length == 6) {
				temperature = Integer.parseInt(line.substring(5, 6));
			} else if (length == 7) {
				temperature = Integer.parseInt(line.substring(5, 7));
			}
			output.collect(new Text(year), new IntWritable(temperature));
		}

	}
/*
	public static class MaxPartioner implements Partitioner<Text, IntWritable> {

		@Override
		public void configure(JobConf job) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getPartition(Text key, IntWritable value, int numPartitions) {
			// TODO Auto-generated method stub
			return 0;
		}

	}
*/
	public static class MaxTemperatureReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text Key, Iterator<IntWritable> Values, OutputCollector<Text, IntWritable> output, Reporter arg3) throws IOException {

			int maxValue = Integer.MIN_VALUE;
			while (Values.hasNext()) {
				int temperature = Values.next().get();
				if (maxValue < temperature) {
					maxValue = temperature;
				}
			}
			output.collect(Key, new IntWritable(maxValue));
		}

	}

	public static void main(String[] args) throws Exception {

		JobConf conf = new JobConf(MaxTemperature.class);
		conf.setJobName("maxtemp");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(MaxTemperatureMapper.class);
		conf.setReducerClass(MaxTemperatureReducer.class);
	//	conf.setPartitionerClass(MaxPartioner.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);

	}
}