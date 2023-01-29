package com.pimzero.jinterpose.action;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.pimzero.jinterpose.Proto;

public class NoopClassVisitor extends ClassVisitor {
	private Proto.Matcher_expr when;
	private Proto.Matcher.Builder current;

	private Proto.Action.Do.DoNoop config;

	public NoopClassVisitor(
			ClassVisitor cv,
			Proto.Matcher_expr when,
			Proto.Matcher.Builder current,
			Proto.Action.Do.DoNoop config) {
		super(Opcodes.ASM9, cv);

		this.when = when;
		this.current = current;

		this.config = config;
	}
}
