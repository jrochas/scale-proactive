import sys
import os
import re
import traceback

#For data serialization, json import
if sys.version_info[0] == 2 and sys.version_info[1] < 6 :
    import simplejson as json
else:
    import json
#For Windows Guests, registry access
if sys.platform == 'win32':
    if sys.version_info[0] == 2:
        import _winreg as winreg
    if sys.version_info[0] == 3:
        import winreg
from subprocess import *
import socket

#A watch dog to end IPC
EOF_MARKER = "EOF"
PA_MESSAGE_LENGTH = 1000

#HyperV_Helper factory class
class HyperV_Helper_Factory :

    def getHyperV_Helper_instance(url, user, pwd):
        """HyperV_Helper getter. The best suitable
        helper is returned. First tries to get windows
        helper as it doesn't require credentials and
        remote connection."""
        #_test method is not a template. That tightly couple
        #Factory with implementation
        if HyperV_Helper_Windows._test():
            print("HyperV_Helper_Windows instantiated")
            return HyperV_Helper_Windows()
        elif HyperV_Helper_NonWindows._test(url, user, pwd):
            print("HyperV_Helper_NonWindows instantiated")
            return HyperV_Helper_NonWindows(url, user, pwd)
        else:
            raise Exception("Cannot define HyperV_Helper_instance,\
                 specify url, user & pwd in hyperv.py or install guest tools")
    getHyperV_Helper_instance = staticmethod(getHyperV_Helper_instance)

#Parent HyperV_Helper class,
class HyperV_Helper:

    #Here are developers data
    proacBootstrapURL = "bootstrapURL"

#Utility class for HyperV_Runtime
class HyperV_Helper_NonWindows(HyperV_Helper) :

    #To build class path for Java program/IPC
    scriptRoot = os.path.dirname(sys.argv[0])
    utilsClassPath = os.path.abspath(scriptRoot + os.path.sep + ".." + os.path.sep + ".."\
                                     + os.path.sep + "lib" + os.path.sep + "ProActive.jar")

    #Java program function for IPC
    utilsCommandHoldingVM = "holdingVM"
    utilsCommandGetData = "getData"

    #Java program
    utilsCommandEXE = "org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm.HyperVUtils"

    def __init__(self,url,user,pwd):
        self.url = url
        self.user = user
        self.pwd = pwd
        self.initialized = False

    #Return True if compliant environment is found
    def _test(url, user, pwd):
        """Test wether the environment matches
        the requirements for a non windows platform"""
        if url != None and user != None and pwd != None:
            return True
        else:
            return False
    _test = staticmethod(_test)

    #To get mounted NIC's mac address
    def __getMacAddress(self):
        """getMacAddress returns an array filled with every
        detected NIC's mac address on the current computer"""
        proc = None
        if sys.platform == 'win32':
            proc = Popen( args = "ipconfig /all", stdout = PIPE, stderr = PIPE)
        else:
            proc = Popen( args = "/sbin/ifconfig", stdout = PIPE, stderr = PIPE)
        output = proc.communicate()[0].decode("utf-8")
        res = re.findall("(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}",output)
        return res

    #Tries to determine in which virtual machine we are running
    def __fixHoldingVirtualMachine(self):
        """Fix the current holding virtual machine
        field given the mac address. Connect to the
        owning hyper-v and iterates over all virtual
        machines to determine which one contains similar
        NIC"""
        socketServer = None
        try:
            macs = self.__getMacAddress()
            print("NIC's mac found: ",macs)
            socketServer = ProActiveSocketServer(2)
            command = ["java", "-cp", HyperV_Helper_NonWindows.utilsClassPath, HyperV_Helper_NonWindows.utilsCommandEXE, repr(socketServer.getPort()), \
            self.url, self.user, self.pwd, HyperV_Helper_NonWindows.utilsCommandHoldingVM]
            for i, mac in enumerate(macs):
                command.append(mac)
            print("Executing: ", command)
            proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
            result = None
            circuitBroker = 0
            while result == None or len(result) == 0 or circuitBroker < 2:
                result = socketServer.processChildComm()
                if result != None and len(result) > 0:
                    self.holdingVM = result[0]
                    print ("holding vm: ", repr(self.holdingVM))
                    return True
            print("No holdingVM found")
            return False
        finally:
            if socketServer != None:
                socketServer.clean()

    #First attempt to get bootstrapURL to be able to launch PART
    def __fixConfig(self):
        """First attempt to determine PART configuration. Only tries to
        get bootstrapURL data"""
        res = self.__fixHoldingVirtualMachine()
        socketServer = None
        self.config = None
        try:
            if res == True:
                socketServer = ProActiveSocketServer(2)
                command =  ["java", "-cp", HyperV_Helper_NonWindows.utilsClassPath, HyperV_Helper_NonWindows.utilsCommandEXE, repr(socketServer.getPort()), \
                self.url, self.user, self.pwd, HyperV_Helper_NonWindows.utilsCommandGetData, self.holdingVM, HyperV_Helper.proacBootstrapURL]
                print ("Executing: ", command)
                proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
                result = None
                circuitBroker = 0
                while result == None or len(result) == 0 or circuitBroker < 2:
                    result = socketServer.processChildComm()
                    if result != None and len(result) > 0:
                        self.config = json.loads(result[0])
                        print("config loaded: ", repr(self.config))
                        self.initialized = True
                        return self.config
        finally:
            if socketServer != None:
                socketServer.clean()

    #Used to retrieve a data set in virtual machine configuration
    def getData(self,key):
        """To get a data saved thanks to VirtualMachine2#pushData method
        belonging to ProActive Virtual Machine Management Project. One can
        also use native HyperV WMI api to push data. see
        Msvm_VirtualSystemManagementService#ModifyKvpItems method"""
        if self.initialized != None and self.initialized != True:
            self.__fixConfig()
        if self.config != None:
            try:
                data = self.config[key]
                return data
            except KeyError:
                print ("key: ", key, " is not in loaded config...")
        socketServer = None
        try:
            socketServer = ProActiveSocketServer(2)
            command =  ["java", "-cp", HyperV_Helper_NonWindows.utilsClassPath, HyperV_Helper_NonWindows.utilsCommandEXE, repr(socketServer.getPort()), \
            self.url, self.user, self.pwd, HyperV_Helper_NonWindows.utilsCommandGetData, self.holdingVM, key]
            print ("Executing: ", command)
            proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
            result = None
            circuitBroker = 0
            while result == None or len(result) == 0 or circuitBroker < 2:
                circuitBroker = circuitBroker + 1
                result = socketServer.processChildComm()
                if result != None and len(result) > 0:
                    tmpDict = json.loads(result[0])
                    for i, tmpKey in enumerate(tmpDict.keys()):
                        self.config[tmpKey] = tmpDict[tmpKey]
                    return self.config[key]
        finally :
            if socketServer != None:
                socketServer.clean()

#HyperV Helper implementation for Windows Platform
class HyperV_Helper_Windows(HyperV_Helper) :

    #Return True if compliant environment is found
    def _test():
        if sys.platform == 'win32':
            try:
                hkey = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Virtual Machine\\Guest\\Parameters")
                value, type = winreg.QueryValueEx(hkey, "VirtualMachineName")
                if value != None and value != "":
                    return True
                else:
                    return False
            except Exception:
                return False
        else:
            return False
    _test = staticmethod(_test)

    #Used to retrieve a data set in virtual machine configuration
    def getData(self,key):
        """To get a data saved thanks to VirtualMachine2#pushData method
        belonging to ProActive Virtual Machine Management Project. One can
        also use native HyperV WMI api to push data. see
        Msvm_VirtualSystemManagementService#ModifyKvpItems method"""
        hkey = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Virtual Machine\\External")
        try:
            value, type = winreg.QueryValueEx(hkey, key)
            return value
        except Exception:
            return None

#A Socket implementation for IPC between this python program
#and RM Node starter. This socket class is used to communicate
#the new created nodes' url.
class ProActiveSocketServer:

    def __init__(self,nbClients):
        varI = 0
        while varI <= 1000: #circuit broker
            try:
                self.ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.ss.bind(("127.0.0.1",0))
                self.ss.listen(nbClients)
                break
            except Exception:
                varI = varI + 1
                print("An error occured while creating socket server")
                traceback.print_exc()
        print("new ProActiveSocketServer created. Listening on port " + repr(self.ss.getsockname()[1]))

    def processChildComm(self):
        print("PASocketServer is waiting for a connection")
        (cs, address) = self.ss.accept()
        result = []
        varI = 0
        while varI <= 1000: #circuit broker
            received = cs.recv(PA_MESSAGE_LENGTH).decode("utf-8").strip().rstrip()
            if received.find(EOF_MARKER) != -1:
                break
            if len(received) == 0:
                print("A problem occurred while listening for data")
                break
            print("received a new data: " + received)
            result.append(received)
            varI =varI + 1
        cs.close()
        return result

    def getPort(self):
        return self.ss.getsockname()[1]

    def clean(self):
        self.ss.close()
