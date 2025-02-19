package org.ijsberg.iglu.persistence;

import java.io.Serializable;
import java.util.Objects;

public class NestedTestDto implements Serializable {

    private Integer someInteger;
    private String someString;

    public NestedTestDto() {

    }

    public NestedTestDto(Integer someInteger, String someString) {
        this.someInteger = someInteger;
        this.someString = someString;
    }

    public Integer getSomeInteger() {
        return someInteger;
    }

    public String getSomeString() {
        return someString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NestedTestDto that = (NestedTestDto) o;
        if (!Objects.equals(someInteger, that.someInteger)) return false;
        return Objects.equals(someString, that.someString);
    }
}
