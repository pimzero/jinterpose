package com.pimzero.jinterpose;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;

import java.util.HashMap;

import com.pimzero.jinterpose.FieldInterpositionClassVisitor;

import com.pimzero.jinterpose.Proto;

import java.lang.System;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.IllegalArgumentException;

import com.google.protobuf.TextFormat;

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
							var tmp = itr.getFieldInterposition();
							cv = new FieldInterpositionClassVisitor(
								cw, action.getWhen(), current,
								tmp.getSrcOwner(), tmp.getSrcName(),
								tmp.getDstOwner(), tmp.getDstName());
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
