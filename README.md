# Java Logic Sim - A digital logic simulation written in Java

I think the title already tells you a lot about what this project is. But if it does not, here a short explanation: This
program allows you to use nodes and links to build up logical circuits consisting of two base building blocks, the "And"
and "Not" blocks. Those two blocks allow you to build up more complex so-called "blueprints", which can also contain
other blueprints or base blocks. This system allows you to build gigantic chips, compressed down into a single node.
Another very important part are input and output nodes, which are used to communicate with the outside world so to
speak. So you can build a chip with inputs "A" and "B", and output something on output "Out", and when you compress and
use the blueprint, you can simply use those input and output pins, just like with a real chip.

## Wiki

Please check out the [wiki](https://github.com/Scriptor25/LogicSim/wiki) if you want to learn more about how to use the
program or how everything works behind the scenes.

## Work in progress / TODOs

Following things are still work in progress:

- editing blueprints after compressing them
- zooming in and out in the node editor
- (maybe?) a scripting language for building blueprints
