OUT=out/
VENDOR=vendor
VPATH=$(OUT):$(VENDOR)

JAVACFLAGS=-Xlint:deprecation

out/agent.jar: com/pimzero/jinterpose/Agent.class jinterpose.manifest
	jar cvfm $@ jinterpose.manifest -C $(OUT) . -C $(VENDOR) .

# Classes
com/pimzero/jinterpose/Agent.class: \
	com/pimzero/jinterpose/Proto.class \
	com/pimzero/jinterpose/action/FieldInterpositionClassVisitor.class \
	com/pimzero/jinterpose/action/LogMethodClassVisitor.class \
	com/pimzero/jinterpose/action/NoopClassVisitor.class

com/pimzero/jinterpose/Evaluator.class: \
	com/google/protobuf \
	com/pimzero/jinterpose/Proto.class

com/pimzero/jinterpose/Proto.class: \
	com/google/protobuf

# Classes: Actions
com/pimzero/jinterpose/action/FieldInterpositionClassVisitor.class: \
	com/pimzero/jinterpose/Evaluator.class \
	com/pimzero/jinterpose/Proto.class \
	org/objectweb/asm

com/pimzero/jinterpose/action/LogMethodClassVisitor.class: \
	com/pimzero/jinterpose/Evaluator.class \
	com/pimzero/jinterpose/Proto.class \
	org/objectweb/asm

com/pimzero/jinterpose/action/NoopClassVisitor.class: \
	com/pimzero/jinterpose/Proto.class \
	org/objectweb/asm

# Dependencies
ASM_JAR=asm-9.3.jar
$(ASM_JAR):
	wget --no-use-server-timestamps https://repository.ow2.org/nexus/content/repositories/snapshots/org/ow2/asm/asm/9.3-SNAPSHOT/asm-9.3-20220403.091850-24.jar -O $@

PROTOBUF_JAVA_JAR=protobuf-java-3.21.12.jar
$(PROTOBUF_JAVA_JAR):
	wget --no-use-server-timestamps https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.21.12/protobuf-java-3.21.12.jar -O $@

org/objectweb/asm: $(ASM_JAR)
	unzip -D -o $< -d $(VENDOR)

com/google/protobuf: $(PROTOBUF_JAVA_JAR)
	unzip -D -o $< -d $(VENDOR)

# Pattern Rules
%.class: %.java
	mkdir -p $(OUT)
	javac $(JAVACFLAGS) -cp "$(VENDOR):$(OUT)" -d $(OUT) $<

%.java: %.proto
	protoc -I=. --java_out=. $<

# PHONY targets
all: out/agent.jar

clean:
	$(RM) -r $(VENDOR) $(OUT) $(ASM_JAR) $(PROTOBUF_JAVA_JAR)

.PHONY: all clean
