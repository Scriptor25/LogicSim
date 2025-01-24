# Java Logic Sim - A digital logic simulation written in Java

I think the title already tells you a lot about what this project is. But if it does not, here a short explanation: This
program allows you to use nodes and links to build up logical circuits consisting of two base building blocks, the "And"
and "Not" blocks. Those two blocks allow you to build up more complex so-called "blueprints", which can also contain
other blueprints or base blocks. This system allows you to build gigantic chips, compressed down into a single node.
Another very important part are input and output nodes, which are used to communicate with the outside world so to
speak. So you can build a chip with inputs "A" and "B", and output something on output "Out", and when you compress and
use the blueprint, you can simply use those input and output pins, just like with a real chip.

## Work in progress / TODOs

Following things are still work in progress:

- editing blueprints after compressing them
- persistent chip state (?)
- zooming in and out in the node editor
- (maybe) a scripting language for building blueprints

## The blueprint file format (.bff)

This section describes the file format used for storing project data in detail. Every table contains the name, size and
type of each field. Primitives like integers are stored in little endian format, their size can be read from the size
column.

### Primitives

#### UUID

Every UUID consists of two 8-byte integers, for the least and most significant parts. Most significant first, least
second.

| name    | type    | size (in byte) |
|---------|---------|----------------|
| ms bits | integer | 8              |
| ls bits | integer | 8              |

#### Array

Arrays are stored as an integer denoting its number of elements followed by the elements themselves.

| name    | type    | size (in byte) |
|---------|---------|----------------|
| count   | integer | 4              |
| entry 0 |         |                |
| entry 1 |         |                |
| ...     |         |                |
| entry N |         |                |

#### String

A string works just like an array of bytes, containing the characters encoded in the machines character set.

### Context

| name       | type            | size (in byte) |
|------------|-----------------|----------------|
| registry   | registry        |                |
| blueprints | blueprint array |                |

### Registry

| name      | type           | size (in byte) |
|-----------|----------------|----------------|
| functions | function array |                |

### Function

#### Not / And

| name             | type    | size (in byte) |
|------------------|---------|----------------|
| type id = (0, 1) | integer | 4              |
| uuid             | uuid    | 16             |

#### Others

| name         | type              | size (in byte) |
|--------------|-------------------|----------------|
| type id = 2  | integer           | 4              |
| uuid         | uuid              |                |
| inputs       | uuid array        |                |
| outputs      | uuid array        |                |
| instructions | instruction array |                |

### Instruction

| name    | type    | size (in byte) |
|---------|---------|----------------|
| type id | integer | 4              |
| uuid    | uuid    |                |

#### Get Attribute

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 1 | integer | 4              |
| uuid        | uuid    |                |
| attribute   | uuid    |                |

#### Set Attribute

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 2 | integer | 4              |
| uuid        | uuid    |                |
| attribute   | uuid    |                |
| value       | uuid    |                |

#### Get Register

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 3 | integer | 4              |
| uuid        | uuid    |                |
| register    | uuid    |                |
| index       | integer | 4              |

#### Set Register

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 4 | integer | 4              |
| uuid        | uuid    |                |
| register    | uuid    |                |
| index       | integer | 4              |
| value       | uuid    |                |

#### Call

| name        | type       | size (in byte) |
|-------------|------------|----------------|
| type id = 5 | integer    | 4              |
| uuid        | uuid       |                |
| callee      | uuid       |                |
| args        | uuid array |                |

#### Get Result

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 6 | integer | 4              |
| uuid        | uuid    |                |
| call        | uuid    |                |
| index       | integer | 4              |

#### Constant

| name        | type    | size (in byte) |
|-------------|---------|----------------|
| type id = 7 | integer | 4              |
| uuid        | uuid    |                |
| value       | boolean | 1              |

### Blueprint

| name       | type         | size (in byte) |
|------------|--------------|----------------|
| uuid       | uuid         |                |
| label      | string       |                |
| base color | integer      | 4              |
| inputs     | string array |                |
| outputs    | string array |                |
| function   | uuid         |                |
