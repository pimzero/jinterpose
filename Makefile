ENTRY=com.pimzero.classloader.Test
OUT=out/
VENDOR=vendor
VPATH=$(OUT):$(VENDOR)

CLASS=com/pimzero/jinterpose/Agent.class \
      com/pimzero/jinterpose/Proto.class \
      com/pimzero/jinterpose/FieldDescription.class \
      com/pimzero/jinterpose/FieldInterpositionClassVisitor.class

out/agent.jar: $(CLASS) org/objectweb/asm
	jar cvfm $@ jinterpose.manifest -C $(OUT) . -C $(VENDOR) .

com/pimzero/jinterpose/Agent.class: \
	com/pimzero/jinterpose/Proto.class \
	com/pimzero/jinterpose/FieldInterpositionClassVisitor.class

com/pimzero/jinterpose/Proto.class: \
	com/google/protobuf

com/pimzero/jinterpose/Evaluator.class: \
	com/google/protobuf

com/pimzero/jinterpose/FieldInterpositionClassVisitor.class: \
	com/pimzero/jinterpose/Evaluator.class \
	org/objectweb/asm \
	com/google/protobuf

asm-9.3.jar:
	wget https://repository.ow2.org/nexus/content/repositories/snapshots/org/ow2/asm/asm/9.3-SNAPSHOT/asm-9.3-20220403.091850-24.jar -O $@

protobuf-java-3.21.2.jar:
	wget https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.21.2/protobuf-java-3.21.2.jar -O $@


org/objectweb/asm: asm-9.3.jar
	unzip $< -d $(VENDOR)

com/google/protobuf: protobuf-java-3.21.2.jar
	unzip $< -d $(VENDOR)

%.jar:
	jar cvfe $@ $(ENTRY)  $^

%.class: %.java
	mkdir -p $(OUT)
	javac -cp "$(VENDOR):$(OUT)" -d $(OUT) $<

%.java: %.proto
	protoc -I=. --java_out=. $<

clean:
	$(RM) -r $(VENDOR) $(OUT)

.PHONY: clean
