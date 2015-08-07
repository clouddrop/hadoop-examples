package edu.weather;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class MaxofMonthWeatherData {
	/**
	 * Get days which are hotter or colder than today
	 * 
	 * @author samar.kumar
	 * 
	 */
	public static class MaxTemperatureMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

		@Override
		public void map(LongWritable arg0, Text Value, OutputCollector<Text, Text> output, Reporter arg3) throws IOException {

			String line = Value.toString();

			// Example of Input
			// Date Max Min
			// 63891 20130101 5.102 -86.61 32.85 12.8 9.6 11.2 11.6 19.4
			// -9999.00 U -9999.0 -9999.0 -9999.0 -9999.0

			String date = line.substring(6, 14);
			String month = date.substring(4, 6);

			float temp_Max = Float.parseFloat(line.substring(39, 45).trim());
			float temp_Min = Float.parseFloat(line.substring(47, 53).trim());

			output.collect(new Text(month), new Text(String.valueOf(temp_Max + "," + temp_Min)));
		}

	}

	public static class MaxTemperatureReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text Key, Iterator<Text> Values, OutputCollector<Text, Text> output, Reporter arg3) throws IOException {
			float max = Float.NEGATIVE_INFINITY;
			float min = Float.POSITIVE_INFINITY;
			while (Values.hasNext()) {
				String currentValues = ((Text) Values.next()).toString();
				StringTokenizer st = new StringTokenizer(currentValues.toString(), ",");
				Float tempMax = Float.parseFloat(st.nextToken());
				Float tempMin = Float.parseFloat(st.nextToken());
				if(tempMax >max && tempMax != 9999.0){
					max= tempMax;
				}
				if(tempMin < min){
					min = tempMin;
				}
				
			}
			
			output.collect(Key, new Text(max + " ," + min));
		}

	}

	public static void main(String[] args) throws Exception {

		JobConf conf = new JobConf(MaxofMonthWeatherData.class);
		conf.setJobName("weather");
		//conf.set("fs.default.name", "hdfs://localhost:54310");

		// Note:- As Mapper's output types are not default so we have to define
		// the
		// following properties.
		conf.setMapOutputKeyClass(Text.class);
		conf.setMapOutputValueClass(Text.class);

		conf.setMapperClass(MaxTemperatureMapper.class);
		conf.setReducerClass(MaxTemperatureReducer.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);

	}
}
