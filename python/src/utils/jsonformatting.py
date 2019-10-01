import json

obj = '["{"object": 0, "type": "triangle", "x": 203, "y": 256, "probability": "99"}, {"object": 1, "type": "rectangle", "x": 105, "y": 78, "probability": "91"}, "]'
new = obj[2:-2]
arr = new.split(', {')
obj = arr[0]
accept_json = obj.replace("'", "\"")
json_obj = json.loads(accept_json)
print(json_obj["object"])

