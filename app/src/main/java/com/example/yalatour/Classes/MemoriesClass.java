package com.example.yalatour.Classes;

import java.util.List;

public class MemoriesClass {
    private String MemoryId;
    private String Memory_UserId;
    private String Memory_TripId;
    private List<String> Memory_Images;

    private List<String> Memory_Videos;
    private List<String> Memory_Texts;

    public MemoriesClass() {
    }

    public MemoriesClass(String memoryId, String memory_UserId, String memory_TripId, List<String> memory_Images, List<String> memory_Videos, List<String> memory_Texts) {
        MemoryId = memoryId;
        Memory_UserId = memory_UserId;
        Memory_TripId = memory_TripId;
        Memory_Images = memory_Images;
        Memory_Videos = memory_Videos;
        Memory_Texts = memory_Texts;
    }

    public String getMemoryId() {
        return MemoryId;
    }

    public void setMemoryId(String memoryId) {
        MemoryId = memoryId;
    }

    public String getMemory_UserId() {
        return Memory_UserId;
    }

    public void setMemory_UserId(String memory_UserId) {
        Memory_UserId = memory_UserId;
    }

    public String getMemory_TripId() {
        return Memory_TripId;
    }

    public void setMemory_TripId(String memory_TripId) {
        Memory_TripId = memory_TripId;
    }

    public List<String> getMemory_Images() {
        return Memory_Images;
    }

    public void setMemory_Images(List<String> memory_Images) {
        Memory_Images = memory_Images;
    }

    public List<String> getMemory_Videos() {
        return Memory_Videos;
    }

    public void setMemory_Videos(List<String> memory_Videos) {
        Memory_Videos = memory_Videos;
    }

    public List<String> getMemory_Texts() {
        return Memory_Texts;
    }

    public void setMemory_Texts(List<String> memory_Texts) {
        Memory_Texts = memory_Texts;
    }
}

