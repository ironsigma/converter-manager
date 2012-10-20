# Conversion manager.

Your one stop bean conversion.

## Quick start

1. Write converter
2. Register converter
3. Convert beans

### 1. Write a converter

A converter is just a class with one or more methods annotated with 
@Converter that take an object and return an object of a different type.
Optionally it can take additional arguments.

	:::java
	public MyConverter {
	  @Converter
	  public YourObject convert(MyObject object) {
	  	YourObject yo = new YourObject();
	  	yo.setAttr(object.getAttr());
	  	return yo;
	  }
	  
	  @Converter
	  public MyObject revert(YourObject object) {
	  	...
	  }
	  
	  @Converter
	  public MyObject revertConditionally(YourObject object, Boolean includeSubObjects) {
	  	...
	  }
	}

### 2. Register a converter

Create a Conversion Manager object to hold the converters and register
the new converter.

	:::java
	ConverterManager converterManager = new ConverterManager();
	converterManager.register(new MyConverter());

If you are using Spring you can add a converter manager bean and list
each one of the converts.

	:::html
	<bean id="converterManager" class="com.example.app.ConverterManager">
		 <property name="converters">
	    <list>
	    	 <bean class="com.example.app.MyConverter" />
	    </list>
	  </property>  
	</bean>

### 3. Convert beans

Once the converters are registered, use the converter manager bean
to perform the conversions.

	:::java
	YourObject yourObj = converterManager.convert(myObject, YourObject.class);
 
The `convert` method will look through the list of registered
converters, if a converter with matching types is found it will perform
the conversion.

The `convert` method will try to search for a converter of the matching
object type, if nothing is found it will then try to match on the
interface(s) type, and finally on the superclass type.

If no matching converter was found a `ConversionFailedException`
is thrown.

If a converter takes additional arguments they are passed during
the `convert` call.

	:::java
	converterManager.convert(myObject, TargetType.class, arg1);
	
## To do
* More unit testing

## Copyright
Copyright 2012 Juan D Frias

## License
Apache License Version 2.0