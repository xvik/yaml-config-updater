package ru.vyarus.yaml.config.updater.parse.struct

import ru.vyarus.yaml.config.updater.parse.struct.model.YamlStruct
import ru.vyarus.yaml.config.updater.parse.struct.model.YamlStructTree
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 29.05.2021
 */
class TreePathsTest extends Specification {
    def "Check simple parse"() {

        when: "parsing"
        YamlStructTree tree = StructureReader.read(new File(getClass().getResource('/common/sample.yml').toURI()))

        then: "generating leaf paths"
        toPaths(tree) == """   5| prop1/prop1.1
   7| prop1/prop1.2
  12| prop2/prop2.1
  17| prop3"""
    }

    def "Check lists parse"() {

        when: "parsing"
        YamlStructTree tree = StructureReader.read(new File(getClass().getResource('/common/lists.yml').toURI()))

        then: "generating leaf paths"
        toPaths(tree) == """   2| simple_list[0]
   5| simple_list[1]
   7| simple_list[2]
   8| simple_list[3]
   9| simple_list[4]
  13| object[0]/one
  14| object[0]/two
  16| object[1]/one
  17| object[1]/two
  22| object2[0]/one
  23| object2[0]/two
  26| object2[1]/one
  27| object2[1]/two
  31| object3[0]/one
  33| object3[0]/two/three
  34| object3[0]/two/four
  36| object3[0]/and[0]
  37| object3[0]/and[1]
  42| map_of_maps/one/a1
  43| map_of_maps/one/a2
  45| map_of_maps/two/b1
  46| map_of_maps/two/b2
  50| sublist[0]/one/sub1
  51| sublist[0]/two"""
    }

    def "Check multiline parse"() {

        when: "parsing"
        YamlStructTree tree = StructureReader.read(new File(getClass().getResource('/common/multiline.yml').toURI()))

        then: "generating leaf paths"
        toPaths(tree) == """   1| simple
   4| quoted
   7| quoted2
  10| include_newlines
  16| middle_newlines
  24| fold_newlines
  30| ignore_ind
  35| append_ind
  40| custom_indent
  47| custom_indent2
  53| object/sub
  58| list[0]
  60| list[1]/obj
  62| list[2]/ob2
  66| flow"""
    }

    private String toPaths(YamlStructTree tree) {
        return toPaths(tree.getTreeLeaves()).join('\n')
    }

    private List<String> toPaths(List<YamlStruct> props) {
        List<String> res = []
        for(YamlStruct prop: props) {
            if (prop.hasListValue()) {
                // processing list items
                prop.getChildren().each { res.addAll(toPaths(it.getAllPropertiesIncludingScalarLists())) }
            } else {
                res.add(String.format("%4s| ", prop.getLineNum()) + prop.getYamlPath());
            }
        }
        return res;
    }
}