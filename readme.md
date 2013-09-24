Example project of spray on heroku using a streamed Http response to complete the requests.

To run that example you'll need an heroku account and run the following:

    git push heroku master

Once the server is started, to simulate the failing client you'll need to start a curl command and stop it using `CTRL+C`

    curl -N http://<your_deployment>.herokuapp.com/test/$timeout
    ie. curl -N http://<your_deployment>.herokuapp.com/test/60

The `$timeout` parameter is an integer used to simulate the time taken by the computation (the time is in seconds).

The server will just wait for that amount of time before sending back the final response.

The client will receive intermediate CHUNKs containing a `'\0'` character every 500ms to keep the connection alive and detect the peer connection closed.
