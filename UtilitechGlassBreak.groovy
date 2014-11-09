/**
 *  Utilitech Glass Break Sensor
 *
 *  Author: Adam Heinmiller
 *
 *  Date: 2014-11-09
 */

metadata 
{
    definition (namespace: "adamheinmiller", name: "Utilitech Glass Break Sensor", author: "Adam Heinmiller") 
    {
        capability "Contact Sensor"
		capability "Battery"
        
        fingerprint deviceId:"0xA102", inClusters:"0x20, 0x9C, 0x80, 0x82, 0x84, 0x87, 0x85, 0x72, 0x86, 0x5A"
   }

    simulator 
    {
/*
		status "Contact Open":  "command: 7105, payload: 07 FF 00 FF 07 02 00 00"
        status "Contact Closed": "command: 7105, payload: 07 00 00 FF 07 02 00 00"
        
        status "External Sensor Open":  "command: 7105, payload: 07 FF 00 FF 07 FE 00 00"
        status "External Sensor Closed": "command: 7105, payload: 07 00 00 FF 07 FE 00 00"
 
        status "Case Opened":  "command: 7105, payload: 07 FF 00 FF 07 03 00 00"
        status "Case Closed": "command: 8407, payload: 0"
        status "Battery Status": "command: 8003, payload: 1F"
*/
    }

    tiles 
    {
        standardTile("contact", "device.contact", width: 2, height: 2) 
        {
            state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
            state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
        }
        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") 
        {
            state "battery", label:'${currentValue}% battery', unit:""
        }
        
        main "contact"
        details(["contact", "battery"])
    }
}

def installed()
{
	state.BatteryLevel = [100, 100, 100] as int[]

	updated()
}

def updated()
{

}

def getTimestamp() 
{
    return new Date().time
}

def getBatteryLevel(int pNewLevel)
{
	def bl = state.BatteryLevel

	def iAvg = 4 + ((int)(pNewLevel + bl[0] + bl[1] + bl[2]) / 4)
    
    state.BatteryLevel = [pNewLevel, bl[0], bl[1]]
    
    //log.debug "New Bat Level: ${iAvg - (iAvg % 5)}, $state.BatteryLevel" 
    
    return iAvg - (iAvg % 5)
}



def parse(String description) 
{
    def result = []
    
    // "0x20, 0x9C, 0x80, 0x82, 0x84, 0x87, 0x85, 0x72, 0x86, 0x5A"
    
    def cmd = zwave.parse(description)
    
    
    log.debug "Parse:  Desc: $description, CMD: $cmd"
    
    if (cmd) 
    {
/*
		// Did the sensor just wake up?
        if (cmd.CMD == "8407") 
        {
        }
*/        
        result << zwaveEvent(cmd)
	}
    
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) 
{
    logCommand(cmd)

	def result = []
    
    result << createEvent([value: "", descriptionText: "${device.displayName} woke up"])
	result << response(zwave.wakeUpV2.wakeUpNoMoreInformation())

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) 
{
    logCommand(cmd)

    def result = [name: "battery", unit: "%", value: getBatteryLevel(cmd.batteryLevel)]
    
    return createEvent(result)
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) 
{
    logCommand(cmd)
	
    def map = null

    return map
}


def zwaveEvent(physicalgraph.zwave.Command cmd) 
{
    logCommand("**Unhandled**: $cmd")

	return createEvent([descriptionText: "Unhandled: ${device.displayName}: ${cmd}", displayed: false])
}


def logCommand(cmd)
{
	log.debug "Device Command:  $cmd"
}