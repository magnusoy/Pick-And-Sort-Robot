# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
import json
from functools import reduce
import re


class JsonConverter:
    """Converts string containing many objects to an array of Jsons."""

    def __init__(self):
        pass

    def convert_to_json(self, obj):
        """Converts string to json"""
        json_list = []
        data = obj[4: -2]
        arr = self.splitkeepsep(data, '}')
        arr.pop()
        index = 0
        for json_obj in arr:
            accept_json = json_obj.replace("'", "\"")
            if index != 0:
                new_json = accept_json[2::]
                json_obj = json.loads(new_json)
            else:
                json_obj = json.loads(accept_json)
            index += 1
            json_list.append(json_obj)
        return json_list

    def splitkeepsep(self, s, sep):
        """Splits the string with the given seperator."""
        return reduce(lambda acc, elem: acc[:-1] + [acc[-1] + elem] if elem == sep else acc + [elem], re.split("(%s)" % re.escape(sep), s), [])


# Example of usage
if __name__ == "__main__":
    jf = JsonConverter()
    # Giving example data
    obj1 = '["{"object": 0, "type": "circle", "x": 306, "y": 260, "probability": "98"}, {"object": 1, "type": "triangle", "x": 259, "y": 65, "probability": "97"}, "]'
    data = jf.convert_to_json(obj1)
    print(data[0]['x'])
