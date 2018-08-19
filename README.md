# Blockchain

## Problem description
 write a web app that accepts GET requests on the

**/address/:bitcoin_addr** route, and responds with a JSON-formatted list

of all unspent transaction outputs for that address, specifying the

following for each such output:

- value

- transaction hash

- output index

You might find our API useful to access that data (documentation at

https://blockchain.info/api/blockchain_api, in particular you'll want

to look at the address/ endpoints), but you are welcome to use any

block explorer or some other alternative source for this information

if you prefer.

Usage example of the app you should develop:

```bash
$ curl http://localhost:8080/address/1Aff4FgrtA1dZDwajmknWTwU2WtwUvfiXa
```

output:
```json
{

 "outputs": [{

     "value": 63871,

     "tx_hash":

"db9b6ff6ba4fd5813fe1ae8980ee30645221e333c0f647bb1fc777d0f58d3e23",

     "output_idx": 1

   }]

}
``` 