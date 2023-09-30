OUT=out/
VENDOR=vendor
TOOLS=tools
VPATH=$(OUT):$(VENDOR)

JAVACFLAGS=-Xlint:deprecation
#USE_SYSTEM_PROTOC=1

# Dependencies
PROTOBUF_VER=23.1
ASM_VER=9.5

all: out/agent.jar

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

# Protobuf compiler
PROTOC_ZIP=protoc-$(PROTOBUF_VER).zip

ifdef USE_SYSTEM_PROTOC
PROTOC=$(shell which protoc)
else
PROTOC=$(TOOLS)/protoc-$(PROTOBUF_VER)/bin/protoc
$(PROTOC_ZIP):
	wget --no-use-server-timestamps https://github.com/protocolbuffers/protobuf/releases/download/v$(PROTOBUF_VER)/protoc-$(PROTOBUF_VER)-linux-x86_64.zip -O $@

$(PROTOC): $(PROTOC_ZIP)
	mkdir -p $(TOOLS)/protoc-$(PROTOBUF_VER)
	unzip -D -o $< -d $(TOOLS)/protoc-$(PROTOBUF_VER)
endif

# Setup dependencies
ASM_JAR=asm-$(ASM_VER).jar
$(ASM_JAR):
	wget --no-use-server-timestamps https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm/$(ASM_VER)/$(ASM_JAR) -O $@

PROTOBUF_JAVA_VER=3.$(PROTOBUF_VER)
PROTOBUF_JAVA_JAR=protobuf-java-$(PROTOBUF_JAVA_VER).jar
$(PROTOBUF_JAVA_JAR):
	wget --no-use-server-timestamps https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/$(PROTOBUF_JAVA_VER)/$(PROTOBUF_JAVA_JAR) -O $@

org/objectweb/asm: $(ASM_JAR)
	unzip -D -o $< -d $(VENDOR)

com/google/protobuf: $(PROTOBUF_JAVA_JAR)
	unzip -D -o $< -d $(VENDOR)

# Pattern Rules
%.class: %.java $(PROTOC)
	mkdir -p $(OUT)
	javac $(JAVACFLAGS) -cp "$(VENDOR):$(OUT)" -d $(OUT) $<

%.java: %.proto
	$(PROTOC) -I=. --java_out=. $<

clean:
	$(RM) -r $(VENDOR) $(TOOLS) $(OUT) $(ASM_JAR) $(PROTOBUF_JAVA_JAR) $(PROTOC_ZIP)

.PHONY: all clean
