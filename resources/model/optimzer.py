import tensorflow as tf
from tensorflow.python.tools import optimize_for_inference_lib
from tensorflow.python.tools import freeze_graph

inputGraph = tf.GraphDef()
with tf.gfile.Open('frozen_inference_graph.pb', "rb") as f:
  data2read = f.read()
  inputGraph.ParseFromString(data2read)
  
outputGraph = optimize_for_inference_lib.optimize_for_inference(
              inputGraph,
              ["image_tensor"], # an array of the input node(s)
              ["detection_classes"], # an array of output nodes
              tf.int32.as_datatype_enum)

# Save the optimized graph'test.pb'
f = tf.gfile.FastGFile('OptimizedGraph.pb', "w")
f.write(outputGraph.SerializeToString()) 