# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
from keras.preprocessing import image
from keras.models import model_from_json
import numpy as np
import glob


class Model:

    def __init__(self):
        self.test = "Model"



def load_model():
    """Load trained model."""
    json_file = open('.../../../../../resources/model/model.json', 'r')
    loaded_model_json = json_file.read()
    json_file.close()
    loaded_model = model_from_json(loaded_model_json)
    # load weights into new model
    loaded_model.load_weights('.../../../../../resources/model/model.h5')
    print("Loaded model from disk")
    return loaded_model
