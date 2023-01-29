package com.pimzero.jinterpose.action;

import java.lang.System;
import java.text.MessageFormat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.pimzero.jinterpose.Proto;
import com.pimzero.jinterpose.Evaluator;

public class LogMethodClassVisitor extends ClassVisitor {
	private Proto.Matcher_expr when;
	private Proto.Matcher.Builder current;

	private Proto.Action.Do.DoLogMethod config;

	public LogMethodClassVisitor(
			ClassVisitor cv,
			Proto.Matcher_expr when,
			Proto.Matcher.Builder current,
			Proto.Action.Do.DoLogMethod config) {
		super(Opcodes.ASM9, cv);

		this.when = when;
		this.current = current;

		this.config = config;
	}

	@Override
	public MethodVisitor visitMethod(
			int access,
			java.lang.String name,
			java.lang.String descriptor,
			java.lang.String signature,
			java.lang.String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

		current.setMethodname(name);

		if (((access & Opcodes.ACC_FINAL) != 0) && Evaluator.eval(when, current.build())) {
			switch (this.config.getOutputStream()) {
				case STDOUT:
					mv.visitFieldInsn(Opcodes.GETSTATIC,
							"java/lang/System",
							"out",
							"Ljava/io/PrintStream;");
					break;
				case STDERR:
					mv.visitFieldInsn(Opcodes.GETSTATIC,
							"java/lang/System",
							"err",
							"Ljava/io/PrintStream;");
					break;
				default:
					throw new IllegalArgumentException("Unsupported output_stream");
			}

			mv.visitLdcInsn(MessageFormat.format(LogMethodClassVisitor.this.config.getFormat(),
						/* 0 */ this.current.getClassname(),
						/* 1 */ name,
						/* 2 */ descriptor,
						/* 3 */ signature
						));

			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					   "java/io/PrintStream",
					   "println",
					   "(Ljava/lang/String;)V", false);

			return new MethodVisitor(Opcodes.ASM9, mv) {
				@Override
				public void visitMaxs(int maxStack, int maxLocals) {
					mv.visitMaxs(Math.max(maxStack, 2), maxLocals);
				}
			};
		}

		return mv;
	}
}
