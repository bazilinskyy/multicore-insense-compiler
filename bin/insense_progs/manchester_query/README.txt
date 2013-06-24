Example query is for 3 nodes, 0, 1, and 2.

Each node runs part of the query entitled simple-query-N where N is the node number

Node 1 collects temperature & humidity data 
Node 2 collects temperature data only
Node 0 joins the data provided by nodes 1 & 2 and delivers the data to the user.

The join joins two streams:


             Node 1               Node 2
			----				----
			|  |                |  |
			|  |                |  |
		    ----				----
		    | |                  |  
  stream<T> V V stream<S>        V stream<T>
		    ------------------------
			|                      |    Node 0
			|                      |
		    ------------------------
		    		 |
		    		 V
		    		 stream<T concat S>
		    		 
The join does a cross product with a window size of 1 epoch therefore the output from the query is a tuple:
		evalEpoch, epoch, temperature, , humidity
		
In the example we have:

type InflowTuple is struct(integer evalEpoch ; integer epoch ; real humidityVal)	- T above
type OutflowTuple is struct(integer evalEpoch; integer epoch; real temperatureVal)	- S above
type JoinTuple is struct(integer evalEpoch; integer epoch; real humidityVal; real temperatureVal) 

