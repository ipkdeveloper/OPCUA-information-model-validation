DefaultXML: die XML-Datei, die als erstes angezeigt werden soll.

DefaultXML setzen mittels Argument
1.	java –jar DeviationAnalyse.jar –f LogFile20191104_2259.xml

DefaultXML setzen mittels config.ini
1.	DefaultXML in config.ini setzen: DefaultXML=<file>
1.	Normales Starten von DeviationAnalyse.jar

Kein DefaultXML setzen
1.	Config.ini modifizieren: DefaultXML=<file> zu DefaultXML=
2.	Normales Starten von DeviationAnalyse.jar