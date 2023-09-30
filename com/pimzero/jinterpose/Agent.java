package com.pimzero.jinterpose;

import com.google.protobuf.TextFormat;
import com.pimzero.jinterpose.Proto;
import com.pimzero.jinterpose.action.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.IllegalArgumentException;
import java.lang.System;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

class Agent {
	public static void premain(String agentArgs, Instrumentation inst) throws Exception {
		if (agentArgs == null) {
			System.err.println("Missing config");
			return;
		}

		var builder = Proto.Configuration.newBuilder();
		TextFormat.Parser.newBuilder().build().merge(new InputStreamReader(new FileInputStream(agentArgs)), builder);
		var config = builder.build();

		for (var action: config.getActionList()) {
			System.out.println(action.toString());

			inst.addTransformer(new ClassFileTransformer() {
				public byte[] transform(ClassLoader loader,
						String className,
						Class classBeingRedefined,
						ProtectionDomain protectionDomain,
						byte[] classfileBuffer)
						throws IllegalClassFormatException {

					var cr = new ClassReader(classfileBuffer);
					var cw = new ClassWriter(0);

					var current = Proto.Matcher.newBuilder().setClassname(className);

					ClassVisitor cv = cw;

					for (var itr: action.getDoList()) {
						switch (itr.getActionCase()) {
						case FIELD_INTERPOSITION:
							cv = new FieldInterpositionClassVisitor(
								cw, action.getWhen(), current,
								itr.getFieldInterposition());
							break;
						case LOG_METHOD:
							cv = new LogMethodClassVisitor(
								cw, action.getWhen(), current,
								itr.getLogMethod());
							break;
						case FIELD_NEW:
							cv = new FieldNewClassVisitor(
								cw, action.getWhen(), current,
								itr.getFieldNew());
							break;
						case NOOP:
							cv = new NoopClassVisitor(
								cw, action.getWhen(), current,
								itr.getNoop());
							break;
						default:
							throw new IllegalArgumentException();
						}

					}

					cr.accept(cv, 0);

					return cw.toByteArray();
				}
			});
		}
	}
}
