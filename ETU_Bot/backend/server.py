from flask import Flask, request
from flask_cors import CORS, cross_origin

app = Flask(__name__, static_url_path='')
app.config['CORS_HEADERS'] = 'Content-Type'
cors = CORS(app, resources={'*':{'origins': 'http://127.0.0.1:3000'}})

@app.route("/members")
@cross_origin()
def members():
    return {"members" : ["1", "2"]}


@app.route("/hello", methods=['POST', 'GET', 'OPTIONS'], strict_slashes=False)
@cross_origin()
def hello():
    if request.method != 'POST':
        print("Not post")
        return {"members" : ["3", "4"]}
    request_data = request.get_json(force=True)
    print(request_data)
    return {"members" : ["5", "6"]}


if __name__ == "__main__":
    app.run(debug=True)