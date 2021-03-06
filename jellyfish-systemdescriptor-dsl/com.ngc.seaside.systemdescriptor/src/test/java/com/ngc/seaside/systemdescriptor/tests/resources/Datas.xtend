/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.systemdescriptor.tests.resources

import static com.ngc.seaside.systemdescriptor.tests.resources.ParsingTestResource.resource;
import org.eclipse.emf.ecore.resource.Resource
import java.util.Arrays
import org.eclipse.xtext.testing.util.ResourceHelper
import java.util.ArrayList
import java.util.Collection

class Datas {
	public static final ParsingTestResource EMPTY_DATA = resource(
		'''
			package foo
			
			data EmptyData {
			}
		'''
	)

	public static final ParsingTestResource DATE = resource(
		'''
			package clocks.datatypes
			
			data Date {
				int day
				int month
				int year
			}
		'''
	)
	
	public static final ParsingTestResource ALL_DAY_APPOINTMENT = resource(
		'''
			package clocks.datatypes
			
			import clocks.datatypes.Date
			
			data AllDayAppointment extends Date {
				string name
			}
		''',
		DATE
	)

	public static final ParsingTestResource TIME = resource(
		'''
			package clocks.datatypes
			
			data Time {
				int hour {
					metadata {
						"validation": {
							"min": 0,
							"max": 23
						}
					}
				}
				int minute {
					metadata {
						"validation": {
							"min": 0,
							"max": 59
						}
					}
				}
				int second {
					metadata {
						"validation": {
							"min": 0,
							"max": 59
						}
					}
				}
			}
		'''
	)

	public static final ParsingTestResource TIME_ZONE = resource(
		'''
			package clocks.datatypes
			
			enum TimeZone {
				CST MST EST PST
			}
		'''
	)

	public static final ParsingTestResource TIME_CONVENTION = resource(
		'''
			package clocks.datatypes

			enum TimeConvention {
				TWELVE_HOUR TWENTY_FOUR_HOUR
			}
		'''
	)

	public static final ParsingTestResource DATE_TIME = resource(
		'''
			package clocks.datatypes
			
			import clocks.datatypes.Date
			import clocks.datatypes.Time
			
			data DateTime {
				Date date
				many Time time
			}
		''',
		TIME,
		DATE
	)

	public static final ParsingTestResource ZONED_TIME = resource(
		'''
			package clocks.datatypes
			
			import clocks.datatypes.TimeZone
			import clocks.datatypes.DateTime
			
			data ZonedTime {
				DateTime dataTime
				TimeZone timeZone
			}
		''',
		TIME_ZONE,
		DATE_TIME
	)

	public static final ParsingTestResource ALARM_ACKNOWLEDGEMENT = resource(
		'''
			package clocks.datatypes
			
			data AlarmAcknowledgement {
			}
		'''
	)

	public static final ParsingTestResource ALARM_STATUS = resource(
		'''
			package clocks.datatypes
			
			data AlarmStatus {
			}
		'''
	)

	public static final ParsingTestResource DATA_WITH_GENERIC_METADATA = resource(
		'''
			package blah
			
			data Foo { 
				metadata {
				  "name" : "test",
				  "description" : "A test metadata object",
				  "arraydata" : ["metadata", "test"],
				  "boolvalue" : true
				}  
			}	
		'''
	)

	public static final ParsingTestResource DATA_WITH_MANY_FIELDS = resource(
		'''
			package blah
			
			data LotsOfManys { 
				many int x
				many int y
				many int z 
			}	
		'''
	)

	public static final ParsingTestResource FOOD = resource(
		'''
			package food
			
			data Food {
				int calories {
					metadata {
						"maxPerMeal": 500
					}
				}
			}
		'''
	)

	public static final ParsingTestResource HAMBURGER = resource(
		'''
			package food
			
			import food.Food
			
			data Hamburger extends Food {
				boolean withFries
			}
		''',
		FOOD
	)

	public static final ParsingTestResource SLIDERS_MEAL = resource(
		'''
			package food
			
			import food.Hamburger
			
			data SlidersMeal extends Hamburger {
				many Hamburger smallBurgers
			}
		''',
		HAMBURGER
	)

	def static Resource allOf(ResourceHelper resourceHelper, ParsingTestResource... resources) {
		return ParsingTestResource.preparedForParse(resourceHelper, Arrays.asList(resources))
	}

	def static Resource allOf(ResourceHelper resourceHelper, Object... resources) {
		val all = new ArrayList()
		for (Object o : resources) {
			if (o instanceof ParsingTestResource) {
				all.add(o)
			} else if (o instanceof Collection<?>) {
				all.addAll(asCollectionOfResources(o))
			} else {
				throw new IllegalArgumentException(
					"resources must be a ParsingTestResource or a collection of ParsingTestResources!")
			}
		}
		return ParsingTestResource.preparedForParse(resourceHelper, all)
	}

	@SuppressWarnings("unchecked")
	def private static Collection<ParsingTestResource> asCollectionOfResources(Collection<?> collection) {
		if (!collection.isEmpty()) {
			for (Object o : collection) {
				if (!(o instanceof ParsingTestResource)) {
					throw new IllegalArgumentException(
						"collection must contain only instances of ParsingTestResources!")
				}
			}
		}
		return collection as Collection<ParsingTestResource>
	}
}
