# Deefs - Discrete Event Electric Fleet Simulation

This repository provides a discrete event fleet simulator for electric vehicles. It has been developed to investigate 
the potential of an electric taxi service in Munich. 

## Requirements
- Java JDK 8+
- Maven

## Usage
### Configuration
The main configuration can be set up in `config.properties`:

```properties
#INPUT-FILES
#Input file wich specifies the controller target (CSV)
controller_input_file = input/taxicontroller/active_taxis_one_week.csv
#Input file wich specifies the facilities (XML)
facility_input_file = input/facilities/facilities_now.xml
#Input file wich specifies the customer demand (CSV)
demand_input_file = input/demand/demand_list_simulation.csv
#Input file wich specifies the fleet (XML)
fleet_input_file =  input/fleet/fleet_eGolf_VarII_400.xml

#GRAPHHOPPER
#Folder where the routing graph is/should be stored
graphhopper_folder_graph = input/osm/graph/
#Underlying OSM-Network-File
graphhopper_osm_file = input/osm/bayern-latest.osm.pbf

#OUTPUTS
#Output Folder path
output_folder = output/simulation/Simulation/
#Name of output Sqlite database
db_name = eGolf_VarII_II

#CHARGINGPOINT
#Interval the cars SOC schould be updated in ms
chargingpoint_update_interval = 60000
#timestep used for the calculation of the charging curve in ms
chargingpoint_charging_curve_delta_t = 60000

#CONNECTOR 
#time one needs to plug in a car at a charging point in ms
connector_plug_in_time = 180000

#TAXI
#maximum time a car will stay active before it drives back to its home position and logs off in ms
taxi_max_time_active = 36000000
#minimum time a car has to be active before it can be logged off by the taxi controller in ms
taxi_min_time_active = 14400000
#minimum time a car has to stay inactive before it can be logged on again by the taxicontroller
taxi_min_time_inactive = 0

#BEVT
#minimum estimated range a car may not undershot
bevtaxi_remaining_range_min = 15000
#bound for the taxi to decide to go charging (remaining range in m) . 
bevtaxi_remaining_range_recharge = 30000
#minumum soc for a taxi to accept a customer request while recharging in %.
bevtaxi_soc_min_stop_charge = 70
#bound to stop the charging process in %.
bevtaxi_soc_max_stop_charge = 85
#minimum time a taxi has to charge at a charging point in %.
bevtaxi_min_time_charging = 0
#range a taxi looks for the fastest connector in m. If no available chargingpossibility was found in 
#this range, it will choose the next free CP by distance.
#must be bigger than bevtaxi_remaining_range_min
bevtaxi_max_distance_best_connector = 4000

#DEBUG

```

### Set up a simulation scenario
The simulation scenario can be set up defining the following input files:

#### Street Network
Add a OpenStreetMaps network of your investigation area into `input/osm`.
Area dumps are available under https://download.geofabrik.de/

You may consider to further reduce the network: https://smarte-mobilitaet-blog.ftm.mw.tum.de/index.php/2019/09/23/custom-osm-maps-mit-osmium/

#### Demand
Provide a CSV with trips to be run in the simulation. Use Coordinates in WGS84. 

```csv
track_id,day,hour,minute,duration,distance,start_area,start_x,start_y,stop_area,stop_x,stop_y
1,1,13,00,550,6220,1,11.620699056,48.1302652995,2168237,11.6786661784,48.1414794922
2,1,15,00,600,5703,1,11.6403828939,48.1321533203,66115,11.5909667969,48.103914388
3,1,17,00,1200,5465,1,11.6217508952,48.1262369792,65917,11.5655171712,48.1387532552
```


#### Facilites (Cahrgins Stations and Taxi Ranks)
Define ranks and charging stations to be used in this scenario
```xml<?xml version="1.0"?>
      <FACILITIES> 			
      	<FACILITY TYPE="RANK"> 					<!--Facility class RANK-->
      		<ID>1001</ID> 						<!--Unique Facility ID-->
      		<LATITUDE>48.0998</LATITUDE> 		<!--Position latitude-->
      		<LONGITUDE>11.6308</LONGITUDE>  	<!--Position longitude-->
      		<AREA>65954</AREA>              	<!--Position area-->
      		<CAPACITY>6</CAPACITY>   			<!--Rank capacity-->
      		<ADDRESS>Planzeltplatz</ADDRESS> 	<!--Rank address-->
      		<DESCRIPTION>In Perlach</DESCRIPTION> <!--Rank description-->
      	</FACILITY>
      	<FACILITY TYPE="CHARGINGSTATION">       <!--Facility class CHARGINGSTATION-->
      		<ID>2107</ID> 						<!--Unique Facility ID-->
      		<LATITUDE>48.171350</LATITUDE> 		<!--Position latitude-->
      		<LONGITUDE>11.367032</LONGITUDE>  	<!--Position longitude-->
      		<CHARGINGPOINTS> 					<!--List of charginpoints must be at least one-->
      			<CHARGINGPOINT>  				<!--Charging Point 1-->
      				<CONNECTOR> 				<!--One possible connector at charging Point 1-->
      					<TYPE>SCHUKO</TYPE>  	<!--Connector Type [SCHUKO, TYP2, CCS, CHADEMO, SUPERCHARGER]-->
      					<PMAX>3680</PMAX> 		<!--Max. power of this connector in W-->
      				</CONNECTOR>
      				<CONNECTOR> 				<!--Second possible connector at charging Point 1-->
      					<TYPE>TYP2</TYPE>
      					<PMAX>22170</PMAX>
      				</CONNECTOR>
      			</CHARGINGPOINT>
      			<CHARGINGPOINT>       			<!--Charging Point 2-->
      				<CONNECTOR>
      					<TYPE>SCHUKO</TYPE>
      					<PMAX>3680</PMAX>
      				</CONNECTOR>
      				<CONNECTOR>
      					<TYPE>TYP2</TYPE>
      					<PMAX>22170</PMAX>
      				</CONNECTOR>
      			</CHARGINGPOINT>
      		</CHARGINGPOINTS>
      	</FACILITY>
      </FACILITIES>
```

#### Fleet
Define your fleet:

```XML
<FLEET>
	<CAR TYPE = "ICETAXI"> 	<!--Vehicle class-->
		<ID></ID> 			<!--Unique ID-->
		<HOME> 				<!--Home position-->
			<LATITUDE> </LATITUDE> 		<!--Home position lat-->
			<LONGITUDE> </LONGITUDE> 	<!--Home position lon-->
		</HOME>
	</CAR>
	<CAR TYPE="BEVTAXI">
		<ID></ID>
		<HOME>
			<LATITUDE> </LATITUDE>
			<LONGITUDE> </LONGITUDE>
		</HOME>
		<EVCONCEPT> 			<!--EVConcept-->
			<NAME> </NAME> 		<!--Concept name-->
			<E_MEAN> </E_MEAN>
			<BATTERY> 			<!--Battery-->
				<E_BAT_MAX> </E_BAT_MAX>  		<!--Energy max in kWh-->
				<U_BAT> </U_BAT> 				<!--Battery voltage in V-->
				<U_BAT_CELL_N> </U_BAT_CELL_N> 	<!--Cell voltage in V-->
				<U_BAT_CELL_LS> </U_BAT_CELL_LS><!--Ladeschlussspannung in V-->
				<ETA_L> </ETA_L> 				<!--Efficiency-->
			</BATTERY>
			<CHARGINGINTERFACE> 	<!--Charginginterface-->
				<CONNECTOR> 		<!--Connector-->
					<TYPE> </TYPE>  <!--Connector Type [SCHUKO, TYP2, CCS, CHADEMO, SUPERCHARGER]-->
					<PMAX> </PMAX> 	<!--Max power of connector in W-->
				</CONNECTOR>
			</CHARGINGINTERFACE>
		</EVCONCEPT>
	</CAR>
</FLEET>
```

#### Dynamic fleet size
If you want to model a time dependent fleet size, provide a timeseries of available vehicles.
```csv
day;hour;n
1;1;300
1;2;300
1;3;300
1;4;300
```

# Run
```JAVA
public class Main {
	public static void main(String[] args) {
		// define a new simulation scenario
		Scenario scenario = new Scenario();
		
		// Initialize the scenario
		scenario.initialize();
		
		// let the scenario run
		scenario.run();
	}
}
 ```

# Output
Simulation Outputs will be stored in a sqlite Database:




# Related Publications
- Jäger, B., Wittmann, M., & Lienkamp, M. (2017). Agent-based Modeling and Simulation of Electric Taxi Fleets. In 6. Conference on Future Automotive Technology (pp. 11–47).
- Jäger, B., Wittmann, M., & Lienkamp, M. (2016). Analyzing and Modeling a City’s Spatiotemporal Taxi Supply and Demand: A Case Study for Munich. Journal of Traffic and Logistics Engineering, 4(2), 147–153. https://doi.org/10.18178/jtle.4.2.147-153
- Jäger, B., Brickwedde, C., & Lienkamp, M. (2018). Multi-Agent Simulation of a Demand-Responsive Transit System Operated by Autonomous Vehicles. Transportation Research Record: Journal of the Transportation Research Board, 2672(8), 764–774. https://doi.org/10.1177/0361198118786644
 
# Contributions
This Simulation has been developed by Michael Wittmann during his Master's Thesis in 2015:
- Wittmann, M. (2015). Nachfrageorientierte Agentensimulation zur Bestimmung des Elektrifizierungspotentials von Fahrzeugflotten. Technische Universität München.

The Thesis has been supervised by Benedikt Jäger. Benedikt Jäger, was mainly responsible for the definition of the 
research question and made an essential contribution to the overall concept development.  