CC    = g++
INCLUDES          = -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux
CFLAGS         = -c -Wall -fPIC
LDFLAGS        = -shared 
DEBUGFLAGS     = -O0 -D _DEBUG
RELEASEFLAGS   = -O2 -D NDEBUG # -fwhole-program


TARGET   = libSystemUtilities.so
DESTINATION = "../../../../../../../lib/native/$(TARGET)"
SOURCES  = $(wildcard echo src/*.cpp)
HEADERS  = $(wildcard echo include/*.h)
OBJECTS  = $(SOURCES:.cpp=.o)

all: $(SOURCES) $(TARGET) $(DESTINATION)

$(DESTINATION): $(TARGET)
	cp $(TARGET) $(DESTINATION)

$(TARGET): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.cpp.o:
	$(CC) $(INCLUDES) $(CFLAGS) $(RELEASEFLAGS) $< -o $@

clean:
	rm -rf src/*.o
	rm -rf *.so
