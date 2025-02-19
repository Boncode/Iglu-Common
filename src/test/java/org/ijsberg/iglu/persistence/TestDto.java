package org.ijsberg.iglu.persistence;

import java.util.Objects;

public class TestDto /*extends BasicPersistable*/ {

    private long id;

    private String a;
    private Integer b;

    private NestedTestDto nestedTestDto;

    public TestDto() {

    }

    public TestDto(String a, Integer b, NestedTestDto nestedTestDto) {
        this.a = a;
        this.b = b;
        this.nestedTestDto = nestedTestDto;
    }

    public String getA() {
        return a;
    }

    public Integer getB() {
        return b;
    }

    public NestedTestDto getNestedTestDto() {
        return nestedTestDto;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestDto testDto = (TestDto) o;
        if (id != testDto.id) return false;
        if (!Objects.equals(a, testDto.a)) return false;
        if (!Objects.equals(b, testDto.b)) return false;
        return Objects.equals(nestedTestDto, testDto.nestedTestDto);
    }

//    public void setId(long id) {
//        this.id = id;
//    }
}
