package ru.vyarus.yaml.config.updater.merger


import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * @author Vyacheslav Rusakov
 * @since 11.05.2021
 */
class MergerTest extends Specification {

    def "Check simple merge"() {

        setup: "prepare files"
        File current = Files.createTempFile("config", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple.yml').toURI()).toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING)
        File update = Files.createTempFile("update", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple_upd.yml').toURI()).toPath(), update.toPath(), StandardCopyOption.REPLACE_EXISTING)

        when: "merging"
        new Merger(MergerConfig.builder(current, update).backup(false).build()).execute()

        then: "updated"
        current.text == """# something

# something 2
prop1:
  prop1.1: 1.1

  prop1.2: 1.2
  # comment line
  prop1.3: 1.3

# in the middle
prop11:
  prop11.1: 11.1

prop2:

  # sub comment
  prop2.1: 2.1

  list:
    - one
    - two

  obj:
    - one: 1
      two: 2
      three: 3

# comment changed
pppp: some

# complex

# comment
prop3:
  prop3.1: 3.1
"""

        cleanup:
        current.delete()
        update.delete()
    }


    def "Check shifted merge"() {

        setup: "prepare files"
        File current = Files.createTempFile("config", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple.yml').toURI()).toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING)
        File update = Files.createTempFile("update", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple_shifted_upd.yml').toURI()).toPath(), update.toPath(), StandardCopyOption.REPLACE_EXISTING)

        when: "merging"
        new Merger(MergerConfig.builder(current, update).backup(false).build()).execute()

        then: "updated"
        current.text == """# something

# something 2
prop1:
    prop1.1: 1.1

    prop1.2: 1.2
    # comment line
    prop1.3: 1.3

# in the middle
prop11:
    prop11.1: 11.1

prop2:

    # sub comment
    prop2.1: 2.1

    list:
        - one
        - two
  
    obj:
        - one: 1
          two: 2
          three: 3

# comment changed
pppp: some

# complex

# comment
prop3:
    prop3.1: 3.1
"""

        cleanup:
        current.delete()
        update.delete()
    }


    def "Check negative shifted merge"() {

        setup: "prepare files"
        File current = Files.createTempFile("config", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple_shifted_upd.yml').toURI()).toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING)
        File update = Files.createTempFile("update", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/simple.yml').toURI()).toPath(), update.toPath(), StandardCopyOption.REPLACE_EXISTING)

        when: "merging"
        new Merger(MergerConfig.builder(current, update).backup(false).build()).execute()

        then: "updated"
        current.text == """# something

# something 2
prop1:
  prop1.1: 1.1

  prop1.2: 1.2
  # comment line
  prop1.3: 1.3

# in the middle
prop11:
    prop11.1: 11.1

prop2:

  # sub comment
  prop2.1: 2.1

  list:
    - one
    - two
    - three

  obj:
    - one: 22
      two: 22
      three: 3
    - one: 1
      two: 2
      three: 3

# original comment
pppp: some

# complex

# comment
prop3:
    prop3.1: 3.1
"""

        cleanup:
        current.delete()
        update.delete()
    }


    def "Check multiline values merge"() {

        setup: "prepare files"
        File current = Files.createTempFile("config", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/multiline.yml').toURI()).toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING)
        File update = Files.createTempFile("update", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/multiline_upd.yml').toURI()).toPath(), update.toPath(), StandardCopyOption.REPLACE_EXISTING)

        when: "merging"
        new Merger(MergerConfig.builder(current, update).backup(false).build()).execute()

        then: "updated"
        current.text == """object:
    simple: value with
      multiple lines (flow)

    include_newlines: |
      exactly as you see
      will appear these three
      lines of poetry

    middle_newlines: |
      exactly as you see
      will appear these three

      lines of poetry

    sub: |2
        first line
      second line
"""

        cleanup:
        current.delete()
        update.delete()
    }

    def "Check multiline values negative shift merge"() {

        setup: "prepare files"
        File current = Files.createTempFile("config", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/multiline_upd.yml').toURI()).toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING)
        File update = Files.createTempFile("update", ".yml").toFile()
        Files.copy(new File(getClass().getResource('/merge/multiline.yml').toURI()).toPath(), update.toPath(), StandardCopyOption.REPLACE_EXISTING)

        when: "merging"
        new Merger(MergerConfig.builder(current, update).backup(false).build()).execute()

        then: "updated"
        current.text == """object:
  simple: value with
      multiple lines (flow)

  include_newlines: |
      exactly as you see
      will appear these three
      lines of poetry

  middle_newlines: |
      exactly as you see
      will appear these three

      lines of poetry

  sub: |4
        first line
      second line
"""

        cleanup:
        current.delete()
        update.delete()
    }
}
