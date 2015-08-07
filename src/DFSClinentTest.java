import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class DFSClinentTest {

	public static void main(String args[]) throws IOException {

		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://192.168.1.5:8020");
		
		FileSystem fs = FileSystem.get(conf);

		Path inFile = new Path("/tmp/xmlOut/part-00000");
		Path outFile = new Path("/tmp/2");

		if (!fs.exists(inFile))
			System.out.println("Input file not found");

		FSDataInputStream in = fs.open(inFile);

		FSDataOutputStream out = fs.create(outFile);

		int bytesRead = 0;
		byte[] buffer = new byte[100];

		while ((bytesRead = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytesRead);
		}

		in.close();
		out.close();

	}

}
