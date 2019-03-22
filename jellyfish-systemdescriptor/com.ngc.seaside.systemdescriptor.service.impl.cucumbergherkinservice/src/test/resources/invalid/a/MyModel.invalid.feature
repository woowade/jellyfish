#
# UNCLASSIFIED
# Northrop Grumman Proprietary
# ____________________________
#
# Copyright (C) 2019, Northrop Grumman Systems Corporation
# All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains the property of
# Northrop Grumman Systems Corporation. The intellectual and technical concepts
# contained herein are proprietary to Northrop Grumman Systems Corporation and
# may be covered by U.S. and Foreign Patents or patents in process, and are
# protected by trade secret or copyright law. Dissemination of this information
# or reproduction of this material is strictly forbidden unless prior written
# permission is obtained from Northrop Grumman.
#

@my-feature-tag
Feature: A simple example with invalid syntax

   This is a longer description

   Background:
      Given the room has been built

   Scenario Outline: Has an invalid number of columns

      Given Bob is in the room
      And the lights are on
      When <person> enters the "room"
      Then Bob should greet <person> by saying <greeting> with <lastCol>

      Examples:
         | Person  | Greeting | lastCol |
         | Adam    | Hello    |
         | Charles | Hi       | okay    |