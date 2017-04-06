var foxhunt = function(){

// Start position for the map (hardcoded here for simplicity,
// but maybe you want to get from URL params)
var lat=59.12755;
var lon=11.3535;

var centerPosition;
var zoom=18;

var map; //complex object of type OpenLayers.Map

var xmin=0;
var xmax=0;
var ymin=0;
var ymax=0;

var pointList = [];

var foxStore = new Array();
var gameObjects = new Array();

var fetchedLastOne = false;

var retrieveObjectStatusinProgress = false;

var urlState = "./getState?wc";
var urlConfig = "./getConfig";

var layerMarkers;
var layerBounds;

var size1 = new OpenLayers.Size(60,54);
var offset1 = new OpenLayers.Pixel(-(size1.w/2), -size1.h/2);

var foxMarkerSize = new OpenLayers.Size(60,24);
var foxMarkerOffset = new OpenLayers.Pixel(-(foxMarkerSize.w/2), -foxMarkerSize.h/2);

var foxCaughtMarkerSize = new OpenLayers.Size(41,18);
var foxCaughtMarkerOffset = new OpenLayers.Pixel(-(foxMarkerSize.w/2), -foxMarkerSize.h/2);

var hunterMarkerSize = new OpenLayers.Size(52,52);
var hunterMarkerOffset = new OpenLayers.Pixel(-(hunterMarkerSize.w/2), -hunterMarkerSize.h/2);

//var defaultFoxMarker = new OpenLayers.Icon("fox.gif",size1,offset1);
var defaultHunterMarker = new OpenLayers.Icon("hat.png",hunterMarkerSize,hunterMarkerOffset);

var defaultFoxMarker = new OpenLayers.Icon("fox2.png",foxMarkerSize,foxMarkerOffset);
var defaultFoxCaughtMarker = new OpenLayers.Icon("fox-caught.png",foxCaughtMarkerSize,foxCaughtMarkerOffset);

var defaultPredatorMarker = new OpenLayers.Icon("predator.gif",size1,offset1);

var sizeBoxMarker = new OpenLayers.Size(32,32);
var offsetBoxMarker = new OpenLayers.Pixel(-(sizeBoxMarker.w/2), -sizeBoxMarker.h/2);
var defaultBoxMarker = new OpenLayers.Icon("box.gif", sizeBoxMarker, offsetBoxMarker);


/**
 * Add marker to map
 * @param {String|Int} id
 * @param {Float} latitude
 * @param {Float} longitude
 * @param {String} title
 * @param {Boolean} selected
 */
function addMarker (id, lat, lon, title, defaultMarker) {
    gameObjects[id]['marker'] = new OpenLayers.Marker(new OpenLayers.LonLat(lon,lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject()),defaultMarker.clone());
    layerMarkers.addMarker(gameObjects[id]['marker']);

    // Add label:
    gameObjects[id]['marker'].icon.imageDiv.setStyle({overflow: "visible"});
    gameObjects[id]['marker'].icon.imageDiv.setStyle({color: "red"});
    
    gameObjects[id]['marker'].icon.imageDiv.childNodes[0].setStyle({imageOrientation: "90deg"});
    
    gameObjects[id]['marker'].icon.imageDiv.addClassName('mapMarker');
    
    if (gameObjects[id]['classname'] == "Hunter" || gameObjects[id]['classname'] == "TagPlayer")
    {
	    var label = document.createElement('div');
	    label.className = "mapLabel";
	    label.id = "mapLabel:" + id;
	    label.innerHTML = title;
	    label.setStyle({position: "relative", left: "15px", top: "-30px"});
	    gameObjects[id]['marker'].icon.imageDiv.appendChild(label);
    }
}

function retrieveSettings() {
	var url = urlConfig;
	var http_request = false;

	if (window.XMLHttpRequest) { // Mozilla, Safari, ...
	
		http_request = new XMLHttpRequest();
		if (http_request.overrideMimeType) {
		http_request.overrideMimeType('text/xml');
		// See note below about this line
		}
	} else if (window.ActiveXObject) { // IE
		try {
			http_request = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				http_request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {}
		}
	}

	if (!http_request) {
	    alert('Giving up :( Cannot create an XMLHTTP instance');
	    return false;
	}

	http_request.onreadystatechange = function() { getSettings(http_request); };
	http_request.open('GET', url, true);
	http_request.send(null);
}


function retriveObjectStatus() {
	if (retrieveObjectStatusinProgress == true)
	{
	        return;
	}
	
	retrieveObjectStatusinProgress = true;
        
	//var url = 'http://absentia.toppe.net:8080/FoxHuntServlet/getState';
	url = urlState;
	var http_request = false;

	if (window.XMLHttpRequest) { // Mozilla, Safari, ...
		http_request = new XMLHttpRequest();
		if (http_request.overrideMimeType) {
			http_request.overrideMimeType('text/xml');
			// See note below about this line
		}
	} else if (window.ActiveXObject) { // IE
		try {
			http_request = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
			    http_request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {}
		}
	}

	if (!http_request) {
		alert('Giving up :( Cannot create an XMLHTTP instance');
		return false;
	}

	http_request.onreadystatechange = function() { plotObjects(http_request);};
	http_request.open('GET', url, true);
	http_request.send(null);
	
	retrieveObjectStatusinProgress = false;

}

function getSettings(http_request) {

	if (http_request.readyState == 4) {
		if (http_request.status == 200) {
			var xmldoc = http_request.responseXML;
			
			
			// Build up our bounds list
            var bounds = xmldoc.getElementsByTagName('point');
            
            for (i=0;i<bounds.length;i++)
            {
                //Display only element nodes
                if (bounds.item(i).nodeType==1)
                {
                	//alert(bounds.item(i).getAttribute('lat'));
                    var p = new OpenLayers.Geometry.Point(bounds.item(i).getAttribute('lon'),
                                                                   bounds.item(i).getAttribute('lat'));
                                                                   
                    p.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
                    pointList.push(p);
                    centerPosition = p;
                }
            }
            pointList.push(pointList[0]);
			// Set up the game boundary
			/*
			ymin = xmldoc.getElementsByTagName('boundry')[0].getAttribute('ltmin')*1.0;
			ymax = xmldoc.getElementsByTagName('boundry')[0].getAttribute('ltmax')*1.0;
			xmin = xmldoc.getElementsByTagName('boundry')[0].getAttribute('lnmin')*1.0;
			xmax = xmldoc.getElementsByTagName('boundry')[0].getAttribute('lnmax')*1.0;
			*            // Create a rectangle
            var bounds = new OpenLayers.Bounds(xmin, ymin, xmax, ymax);
            var box = new OpenLayers.Marker.Box(bounds);
            box.events.register("click", box, function (e) {
                this.setBorder("yellow");
            });
			*/
			
			map.setCenter (new OpenLayers.LonLat(centerPosition.x, centerPosition.y), zoom);
            //alert("Setting center" + centerPosition);
            var linearRing = new OpenLayers.Geometry.LinearRing(pointList);
            var polygonFeature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon([linearRing]));
			
            layerBounds.addFeatures([polygonFeature]);
			
			// Build up our gameObject list
			var objects = xmldoc.getElementsByTagName('gameObject');
			
			for (i=0;i<objects.length;i++)
			{
				//Display only element nodes
				if (objects.item(i).nodeType==1)
				{
					
					var id= objects.item(i).getAttribute('id');
					gameObjects[id] = new Array();
					gameObjects[id]['name'] = objects.item(i).getAttribute('name');
					gameObjects[id]['classname'] = objects.item(i).getAttribute('class');
	                
					var defMark = defaultFoxMarker;
					
					if (gameObjects[id]['classname'] == "Predator")
					{
					        defMark = defaultPredatorMarker;
					}
					
					if (gameObjects[id]['classname'] == "Obstacle")
                    {
                            defMark = defaultBoxMarker;
                    }
                    
                    if (gameObjects[id]['classname'] == "Hunter")
                    {
                            defMark = defaultHunterMarker;
                    }
                    if (gameObjects[id]['classname'] == "TagPlayer")
                    {
                            defMark = defaultHunterMarker;
                    }
					
					//var theString = '<div id="'+id+'" style="position:absolute;left:0px;top:0px;width:350px;"><img src="' + image +'" height="50" width="50">'+foxName+'</div>';
					
					//var lonLat = new OpenLayers.LonLat(, ).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());

					//var offset = new OpenLayers.Pixel(0, 0);
					   
					addMarker(id, objects.item(i).getAttribute('lt'), objects.item(i).getAttribute('ln'), gameObjects[id]['name'], defMark);
					
					
					//var icon = new OpenLayers.Icon(image,size,offset);
					
					//gameObjects[id]['marker'] = new OpenLayers.Marker(lonLat,icon);
					
					//layerMarkers.addMarker(gameObjects[id]['marker']);
	                  
				}			
			}
		}
	}
}

function plotObjects(http_request) {

	if (http_request.readyState == 4) {
		if (http_request.status == 200) {
			var xmldoc = http_request.responseXML;
			var objects = xmldoc.getElementsByTagName('gameObject');

			var posNode = document.getElementById('positions');
                            
			while(posNode != null && posNode.hasChildNodes()>0)
			{
				posNode.removeChild(posNode.lastChild);
			}

            var tbody = document.getElementById("scores").getElementsByTagName("tbody")[0]; 
            while(tbody.childNodes.length>1) {
                tbody.removeChild(tbody.lastChild);
            }


			for (i=0;i<objects.length;i++)
			{
				//Display only element nodes
				if (objects.item(i).nodeType==1)
				{
	
					var id = objects.item(i).getAttribute('id');
					
					var iscaught = objects.item(i).getAttribute('iscaught');
					var caught = objects.item(i).getAttribute('caught');
					var heading = objects.item(i).getAttribute('heading');
					var ishunter = objects.item(i).getAttribute('ishunter');
					
					var opacity = 1.0;
					
					if (gameObjects[id])
					{
						
						var defMark = defaultFoxMarker;
					
						if (gameObjects[id]['classname'] == "Obstacle")
	                    {
	                            defMark = defaultBoxMarker;
	                    }
                    
						if (gameObjects[id]['classname'] == "Predator")
						{
						        defMark = defaultPredatorMarker;
						}
						
						if (gameObjects[id]['classname'] == "Hunter")
						{
						        defMark = defaultHunterMarker;
						}
						
						if (gameObjects[id]['classname'] == "Fox" && iscaught)
						{
						        defMark = defaultFoxCaughtMarker;
						        opacity=0.4;
						}
					    if (gameObjects[id]['classname'] == "TagPlayer")
                        {
                            defMark = defaultHunterMarker;
                        }
                        if (gameObjects[id]['classname'] == "TagPlayer" && ishunter)
                        {
                            defMark = defaultPredatorMarker;
                        }
                        
						//var lonLat = new OpenLayers.LonLat(objects.item(i).getAttribute('ln'), objects.item(i).getAttribute('lt')).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
						//gameObjects[id]['marker'].moveTo(map.getPixelFromLonLat(lonLat));
						
	                    if (gameObjects[id]['marker'] != null)
	                    {
								layerMarkers.removeMarker(gameObjects[id]['marker']);
	                            gameObjects[id]['marker'].destroy();
	                            delete gameObjects[id]['marker'];
	                            
	                    }
						addMarker(id, objects.item(i).getAttribute('lt'), objects.item(i).getAttribute('ln'), gameObjects[id]['name'], defMark);
					
						gameObjects[id]['marker'].setOpacity( opacity );	
						// 
						
						if (gameObjects[id]['classname'] == "Hunter") {
						    var row = document.createElement("TR"); 
							var cell1 = document.createElement("TD"); 
							cell1.innerHTML = id; 
							var cell2 = document.createElement("TD"); 
							cell2.innerHTML = gameObjects[id]['name']; 
							var cell3 = document.createElement("TD"); 
							cell3.innerHTML = caught; 
							<!-- row.appendChild(cell1);  -->
							row.appendChild(cell2); 
							row.appendChild(cell3); 
							tbody.appendChild(row);
						}
						
						if (gameObjects[id]['classname'] == "TagPlayer") {
                            var row = document.createElement("TR"); 
                            var cell1 = document.createElement("TD"); 
                            cell1.innerHTML = id; 
                            var cell2 = document.createElement("TD"); 
                            cell2.innerHTML = gameObjects[id]['name']; 
                            var cell3 = document.createElement("TD"); 
                            cell3.innerHTML = objects.item(i).getAttribute('speed');
                            var cell4 = document.createElement("TD");
                            cell4.innerHTML = objects.item(i).getAttribute('stamina');
                            <!-- row.appendChild(cell1);  -->
                            row.appendChild(cell2); 
                            row.appendChild(cell3); 
                            row.appendChild(cell4); 
                            tbody.appendChild(row);
                        }
					}
				}			
			}

		} else {
		//alert('There was a problem with the request.');
		}
	}
}

//Initialise the 'map' object
function initMap() {
	
	   var controls = [
                        new OpenLayers.Control.Navigation(),
                        new OpenLayers.Control.PanZoomBar(),
                        new OpenLayers.Control.LayerSwitcher()
                        ];
       
       var config_geonorge_latlon = {
       	                                controls: controls,
       	                                maxExtent: new OpenLayers.Bounds(1.2781194,56.9176113,32.988433,72.1175026),
       	                                maxResolution: "auto",
       	                                
       	                                units: 'm',
       	                                projection: new OpenLayers.Projection("EPSG:4326"),
       	                                displayProjection: new OpenLayers.Projection("EPSG:4326")
       	                                
       }
       
       var config_geonorge_fly = {
                                        controls: controls,
                                        maxExtent: new OpenLayers.Bounds(-120683,6338570,1154623,8055520),
                                        maxResolution: "auto",
                                        
                                        units: 'm',
                                        projection: "EPSG:32633",
                                        displayProjection: new OpenLayers.Projection("EPSG:4326")
                                        
       }
       
        var config_google = {
                                        controls: controls,
                                        maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
                                        numZoomLevels: 18,
                                        maxResolution:156543.0339,
                                        
                                        units: 'm',
                                        projection: new OpenLayers.Projection("EPSG:900913"),
                                        displayProjection: new OpenLayers.Projection("EPSG:4326")
                                        
       }      
       
	
	
        map = new OpenLayers.Map ("map", config_google);
        
        map.addControl(new OpenLayers.Control.MousePosition());
        
        /*
        map.events.register("mousemove", map, function(e) { 
                var position = this.events.getMousePosition(e);
                OpenLayers.Util.getElement("coords").innerHTML = position;
        });
        */
        
        //map = new OpenLayers.Map( 'map' );
        /*
        map.addControl(new OpenLayers.Control.Navigation());
        map.addControl(new OpenLayers.Control.LayerSwitcher());
        map.addControl(new OpenLayers.Control.ScaleLine());
        */

        // Other defined layers are OpenLayers.Layer.OSM.Mapnik, OpenLayers.Layer.OSM.Maplint and OpenLayers.Layer.OSM.CycleMap
        //var tah = new OpenLayers.Layer.OSM("Tiles@Home Local Cache", "http://asia.hiof.no/aleksalt/foxhunt/map/cache/tah/", 
        //        {
        //                numZoomLevels: 18
        //        },
        /*
                {
                        //maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
                        //maxResolution: 156543.0399, // Another alternative is 'auto', which 
                                      // will automatically fit the map: you can 
                                      // then check map.baseLayer.resolutions[0] for
                                      // a reasonable value.
                        //numZoomLevels: 19,
                        //projection:"EPSG:900913",     // Used in WMS/WFS requests.   
                        //units: "m"                  // Only neccesary for working with scales.
                } );
                
           */     
         
        //var tah = new OpenLayers.Layer.OSM("Tiles@Home Local Cache", "http://asia.hiof.no/aleksalt/foxhunt/map/cache/tah/", {numZoomLevels: 18}, {'reproject': true});
        //var mapnik = new OpenLayers.Layer.OSM("Mapnik Local Cache", "http://asia.hiof.no/aleksalt/foxhunt/map/cache/mapnik/", {numZoomLevels: 19});
        
        // http://asia.hiof.no/aleksalt/foxhunt/map/proxy.php?url=
        // http://asia.hiof.no/aleksalt/foxhunt/map/wmsProxy.php?imagetype=jpeg&url=http://wms.geonorge.no/skwms1/wms.norgeibilder/?", 
        var nib = new OpenLayers.Layer.WMS( "Norge i Bilder",  "http://asia.hiof.no/aleksalt/foxhunt/map/wmsProxy.php?imagetype=jpeg&url=http://wms.geonorge.no/skwms1/wms.norgeibilder/?", 
                {
                        layers: 'SatelliteImage,Backdrops,OrtofotoAlle',
                        exceptions: "application/vnd.ogc.se_xml"
                },
                {
                        //maxExtent: new OpenLayers.Bounds(-120683,6338570,1154623,8055520),
                        //maxResolution: "auto",
                        //maxResolution: 0.02,
                        //maxResolution: 156543.0399,
                        //format:OpenLayers.Format.GeoJSON,
                        //projection: new OpenLayers.Projection("EPSG:32633")//,
                        projection: new OpenLayers.Projection("EPSG:4326")
                        //singleTile: 'true'
                        //ratio: '1'
                });
        
        // EPSG:4326
        // EPSG:32633
        //nib.encodeBBOX = true;
        //var vemap = new VEMap("map");
        //var velayer = new OpenLayers.Layer.VirtualEarth( "VE", { minZoomLevel: 4, maxZoomLevel: 20, 'type': VEMapStyle.Aerial});
        
        
        var osm_tah = new OpenLayers.Layer.WMS( "OSM(Cache) Tiles@Home", "http://asia.hiof.no/aleksalt/foxhunt/map/getMap.php?", 
                {
                        layers: 'cache_tah'
                        /*format: 'image/png'*/
                },
                {
                       maxExtent: new OpenLayers.Bounds(1.2781194,56.9176113,32.988433,72.1175026),
                       maxResolution: "auto",
                       projection: new OpenLayers.Projection("EPSG:4326")
                });
                
        var osm_mapnik = new OpenLayers.Layer.WMS( "OSM(Cache) Mapnik", "http://asia.hiof.no/aleksalt/foxhunt/map/getMap.php?", 
                {
                        layers: 'cache_mapnik'
                        /*format: 'image/png'*/
                },
                {
                       maxExtent: new OpenLayers.Bounds(1.2781194,56.9176113,32.988433,72.1175026),
                       //maxResolution: "auto",
                       projection: new OpenLayers.Projection("EPSG:4326")
                });
        
        
        
        
        function get_my_url (bounds) {
        	var bounds2 = bounds.clone();
        	bounds2.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
        	
        	
            var res = 156543.0339;
            var x = Math.round((bounds2.left - mercbound.left) / (res * this.tileSize.w));
            var y = Math.round((mercbound.top - bounds2.top) / (res * this.tileSize.h));
            var z = this.map.getZoom();
            var limit = Math.pow(2, z);

            if (y < 0 || y >= limit) {
                return OpenLayers.Util.getImagesLocation() + "404.png";
            } else {
                x = ((x % limit) + limit) % limit;
                return this.url + z + "/" + x + "/" + y + "." + this.type;
            }
        }
        
       function osm_getTileURL(bounds) {
            var res = this.map.getResolution();
            
            var bounds2 = bounds.clone();
            //bounds2.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
            var mercbound = new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34);
            
            var x = Math.round((bounds2.left - mercbound.left) / (res * this.tileSize.w));
            var y = Math.round((mercbound.top - bounds2.top) / (res * this.tileSize.h));
            var z = this.map.getZoom();
            var limit = Math.pow(2, z);

            if (y < 0 || y >= limit) {
                return OpenLayers.Util.getImagesLocation() + "404.png";
            } else {
                x = ((x % limit) + limit) % limit;
                return this.url + z + "/" + x + "/" + y + "." + this.type;
            }
        }
        
        
       var osm_tilemapnik=  new OpenLayers.Layer.TMS("OpenStreetMap Mapnik - Tileserver",
                       "http://asia.hiof.no/aleksalt/foxhunt/map/cache/mapnik/",
                       { 'type':'png', 'getURL':osm_getTileURL , numZoomLevels: 19});
       
      var osm_tiletah=  new OpenLayers.Layer.TMS("OpenStreetMap TaH - Tileserver",
               "http://asia.hiof.no/aleksalt/foxhunt/map/cache/tah/",
               { 'type':'png', 'getURL':osm_getTileURL , numZoomLevels: 19});
        
        map.addLayer(osm_tilemapnik);
        map.addLayer(osm_tiletah);
        
        //map.addLayer(osm_tah);
        //map.addLayer(osm_mapnik);
        //map.addLayer(nib);
        
        //map.addLayer(tah);
        //map.addLayer(mapnik);
        //map.addLayer(velayer);


        var lonLat = new OpenLayers.LonLat(lon, lat);
        
        //lonLat.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:32633"));

        lonLat.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
        //var lonLatNIB = new OpenLayers.LonLat(291334.0, 6559977.0);
                
        //map.setCenter (lonLat, zoom);
        //vemap.LoadMap(new VELatLong(lonLat.lat, lonLat.lon), map.getZoom(), VEMapStyle.Hybrid);
        
        layerMarkers = new OpenLayers.Layer.Markers("Markers");
        map.addLayer(layerMarkers);
        
        
        var layer_style = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
        layer_style.fillOpacity = 0.05;
        layer_style.strokeColor = "red";

        layer_style.graphicOpacity = 1;
        
        layerBounds = new OpenLayers.Layer.Vector("Boundary", {style: layer_style});
        
        //new OpenLayers.Layer.Boxes( "Bounds" );
        map.addLayer(layerBounds);
        
        
        /*
         *             // create a polygon feature from a linear ring of points
            var pointList = [];
            for(var p=0; p<6; ++p) {
                var a = p * (2 * Math.PI) / 7;
                var r = Math.random(1) + 1;
                var newPoint = new OpenLayers.Geometry.Point(point.x + (r * Math.cos(a)),
                                                             point.y + (r * Math.sin(a)));
                pointList.push(newPoint);
            }
            pointList.push(pointList[0]);
            
            var linearRing = new OpenLayers.Geometry.LinearRing(pointList);
            polygonFeature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon([linearRing]));
         
        map.events.register(”moveend”, map, function(e) {
        var longlat = map.getCenter();
        vemap.SetZoomLevel(map.getZoom() + 9);
        vemap.PanToLatLong(new VELatLong(longlat.lat, longlat.lon));
        });
        */
}

        function init() {
        	initMap();
            retrieveSettings();
            window.setInterval(function() {retriveObjectStatus()}, 700);
        }
        
    // return public pointers to the private methods and 
    // properties you want to reveal
    return {
    	init: init,
    	retrieveSettings: retrieveSettings,
        initMap: initMap,
        retriveObjectStatus: retriveObjectStatus
    }
}();

//K("foxhunt.load",retrieveSettings);