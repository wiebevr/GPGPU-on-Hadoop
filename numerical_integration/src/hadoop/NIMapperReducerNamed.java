package hadoop;

import integration.INumericalIntegrationMulti;
import integration.TrapeziumIntegration;

import java.io.IOException;
import java.net.InetAddress;

import lightLogger.Logger;
import mathFunction.IInterval;
import mathFunction.IIntervalNamed;
import mathFunction.IMathFunction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import stopwatch.StopWatch;
import utils.Intervals;
import utils.MathFunctions;

public class NIMapperReducerNamed {

	public static class NIMapper
			extends
			Mapper<LongWritable, Text, Text, FloatIntervalWritable> {

		private static final Class<NIMapper> CLAZZ = NIMapper.class;

		private StopWatch swPhase = new StopWatch(
				NumericalIntegration.Timer.MAPPHASE.prefix,
				NumericalIntegration.Timer.MAPPHASE.suffix);
		private StopWatch swMethod = new StopWatch(
				NumericalIntegration.Timer.MAPMETHOD.prefix,
				NumericalIntegration.Timer.MAPMETHOD.suffix);

		@Override
		protected void setup(NIMapper.Context context) {
			swPhase.start();
			swMethod.start();
			swMethod.pause();

			Logger.logDebug(CLAZZ,
					"TaskAttemptID: " + context.getTaskAttemptID());
			try {
				Logger.logDebug(CLAZZ, "Hostname: "
						+ InetAddress.getLocalHost().getHostName());
			} catch (Exception e) {
				Logger.logDebug(CLAZZ, "Hostname: unknown");
			}
		}

		@Override
		protected void map(LongWritable key, Text value,
				NIMapper.Context context) throws IOException,
				InterruptedException {
			swMethod.resume();

			IIntervalNamed<String, Float> interval = Intervals.createFloatIntervalNamed(value.toString());
			context.write(new Text(interval.getIdentifier()), new FloatIntervalWritable(interval));
			
			swMethod.pause();
		}

		@Override
		protected void cleanup(NIMapper.Context context) {
			swMethod.stop();
			Logger.log(NumericalIntegration.TIME_LEVEL, CLAZZ,
					swMethod.getTimeString());

			swPhase.stop();
			Logger.log(NumericalIntegration.TIME_LEVEL, CLAZZ,
					swPhase.getTimeString());
		}

	}

	public static class NIReducer extends
			Reducer<Text, FloatIntervalWritable, Text, FloatWritable> {

		private static final Class<NIReducer> CLAZZ = NIReducer.class;

		private StopWatch swPhase = new StopWatch(
				NumericalIntegration.Timer.REDUCEPHASE.prefix,
				NumericalIntegration.Timer.REDUCEPHASE.suffix);
		private StopWatch swMethod = new StopWatch(
				NumericalIntegration.Timer.REDUCEMETHOD.prefix,
				NumericalIntegration.Timer.REDUCEMETHOD.suffix);

		private INumericalIntegrationMulti<Float> integration;

		private IMathFunction<Float> function;

		private int resolution;

		@Override
		protected void setup(NIReducer.Context context) {
			swPhase.start();
			swMethod.start();
			swMethod.pause();

			Logger.logDebug(CLAZZ,
					"TaskAttemptID: " + context.getTaskAttemptID());
			try {
				Logger.logDebug(CLAZZ, "Hostname: "
						+ InetAddress.getLocalHost().getHostName());
			} catch (Exception e) {
				Logger.logDebug(CLAZZ, "Hostname: unknown");
			}
			
			Configuration conf = context.getConfiguration();
			function = MathFunctions.getFunction(conf.get(NumericalIntegration.Argument.FUNCTION.name), conf.get(NumericalIntegration.Argument.EXPONENT.name));
			Logger.logInfo(CLAZZ, "Function: " + conf.get(NumericalIntegration.Argument.FUNCTION.name));
			
			resolution = context.getConfiguration().getInt(NumericalIntegration.Argument.RESOLUTION.name, 0);
			Logger.logInfo(CLAZZ, "Resolution: " + resolution);
			
			integration = new TrapeziumIntegration();
			integration.setFunction(function);
		}

		@Override
		protected void reduce(Text key, Iterable<FloatIntervalWritable> values,
				NIReducer.Context context) throws IOException,
				InterruptedException {
			swMethod.resume();

			float result = 0;
			for (IInterval<Float> value : values) {
				result += integration.getIntegral(value, resolution);
			}
			context.write(key, new FloatWritable(result));

			swMethod.pause();
		}

		@Override
		protected void cleanup(NIReducer.Context context) {
			swMethod.stop();
			Logger.log(NumericalIntegration.TIME_LEVEL, CLAZZ,
					swMethod.getTimeString());

			swPhase.stop();
			Logger.log(NumericalIntegration.TIME_LEVEL, CLAZZ,
					swPhase.getTimeString());
		}

	}
}
